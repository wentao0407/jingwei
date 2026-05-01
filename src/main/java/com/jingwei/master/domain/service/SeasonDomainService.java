package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Season;
import com.jingwei.master.domain.model.SeasonStatus;
import com.jingwei.master.domain.model.SeasonType;
import com.jingwei.master.domain.model.Wave;
import com.jingwei.master.domain.repository.SeasonRepository;
import com.jingwei.master.domain.repository.WaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 季节领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>季节 CRUD 及业务校验（编码唯一、年份类型唯一、日期合法性）</li>
 *   <li>季节关闭控制（关闭后不可在业务单据中选择）</li>
 *   <li>波段 CRUD 及业务校验（季节状态校验、编码唯一）</li>
 * </ul>
 * </p>
 * <p>
 * 关键业务规则：
 * <ul>
 *   <li>同一年份同类型季节不可重复——服装行业每年只有一个春夏季和一个秋冬季</li>
 *   <li>季节可关闭，关闭后不可在业务单据中选择——关闭是季节的自然生命周期终点</li>
 *   <li>季节关闭后其下的波段也不可选用——波段依赖季节，季节关闭则波段随之一并失效</li>
 *   <li>波段为可选功能，不使用波段不影响其他业务</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonDomainService {

    private final SeasonRepository seasonRepository;
    private final WaveRepository waveRepository;

    // ==================== 季节 CRUD ====================

    /**
     * 创建季节
     * <p>
     * 校验规则：
     * <ol>
     *   <li>季节编码全局唯一</li>
     *   <li>同一年份同类型季节不可重复</li>
     *   <li>开始日期必须早于结束日期</li>
     * </ol>
     * </p>
     *
     * @param season 季节实体
     * @return 保存后的季节实体
     */
    public Season createSeason(Season season) {
        // 校验编码唯一性
        if (seasonRepository.existsByCode(season.getCode(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "季节编码已存在");
        }

        // 校验同一年份同类型唯一性
        if (seasonRepository.existsByYearAndType(season.getYear(), season.getSeasonType().name(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS,
                    season.getYear() + "年" + getSeasonTypeName(season.getSeasonType()) + "已存在");
        }

        // 校验日期合法性
        if (season.getStartDate() != null && season.getEndDate() != null
                && !season.getStartDate().isBefore(season.getEndDate())) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "开始日期必须早于结束日期");
        }

        season.setStatus(SeasonStatus.ACTIVE);
        try {
            seasonRepository.insert(season);
        } catch (DuplicateKeyException e) {
            // 并发场景下数据库唯一索引兜底
            log.warn("并发创建季节触发唯一约束: code={}", season.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "季节编码或年份类型组合已存在");
        }

        log.info("创建季节: code={}, name={}, year={}, type={}, id={}",
                season.getCode(), season.getName(), season.getYear(), season.getSeasonType(), season.getId());
        return season;
    }

    /**
     * 更新季节
     * <p>
     * 可更新字段：name, startDate, endDate。
     * 不允许变更编码（code）和年份类型组合（year + seasonType）——
     * 这些是季节的核心标识，变更后会影响关联业务数据。
     * 如需更换年份类型组合，应关闭当前季节后重新创建。
     * </p>
     * <p>
     * 已关闭的季节不允许更新——关闭意味着季节已结束，不应再修改。
     * </p>
     *
     * @param seasonId 季节ID
     * @param name     新名称（可为 null，不更新）
     * @param startDate 新开始日期（可为 null，不更新）
     * @param endDate   新结束日期（可为 null，不更新）
     * @return 更新后的季节
     */
    public Season updateSeason(Long seasonId, String name,
                               java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Season existing = seasonRepository.selectById(seasonId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "季节不存在");
        }

        // 已关闭的季节不允许更新
        if (existing.getStatus() == SeasonStatus.CLOSED) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "已关闭的季节不允许修改");
        }

        if (name != null) {
            existing.setName(name);
        }
        if (startDate != null) {
            existing.setStartDate(startDate);
        }
        if (endDate != null) {
            existing.setEndDate(endDate);
        }

        // 校验日期合法性
        if (existing.getStartDate() != null && existing.getEndDate() != null
                && !existing.getStartDate().isBefore(existing.getEndDate())) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "开始日期必须早于结束日期");
        }

        int rows = seasonRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新季节: id={}", seasonId);
        return seasonRepository.selectById(seasonId);
    }

    /**
     * 关闭季节
     * <p>
     * 关闭是单向操作，不可恢复。关闭后：
     * <ul>
     *   <li>该季节不可在业务单据中选择</li>
     *   <li>其下的波段也不可选用</li>
     * </ul>
     * </p>
     *
     * @param seasonId 季节ID
     */
    public void closeSeason(Long seasonId) {
        Season existing = seasonRepository.selectById(seasonId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "季节不存在");
        }

        if (existing.getStatus() == SeasonStatus.CLOSED) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "季节已关闭");
        }

        existing.setStatus(SeasonStatus.CLOSED);
        int rows = seasonRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("关闭季节: id={}, code={}", seasonId, existing.getCode());
    }

    /**
     * 删除季节
     * <p>
     * 仅允许删除未被业务单据引用的季节。
     * 同时删除季节下的所有波段。
     * </p>
     *
     * @param seasonId 季节ID
     */
    public void deleteSeason(Long seasonId) {
        Season existing = seasonRepository.selectById(seasonId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "季节不存在");
        }

        // 检查是否被业务单据引用（当前订单模块尚未实现，预留钩子）
        long orderCount = countOrderReferences(seasonId);
        if (orderCount > 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该季节已被" + orderCount + "个订单引用，不可删除");
        }

        // 先删除季节下的所有波段
        List<Wave> waves = waveRepository.selectBySeasonId(seasonId);
        for (Wave wave : waves) {
            waveRepository.deleteById(wave.getId());
        }

        // 再删除季节
        seasonRepository.deleteById(seasonId);
        log.info("删除季节及{}个波段: id={}, code={}", waves.size(), seasonId, existing.getCode());
    }

    /**
     * 查询季节列表（支持按年份、类型、状态筛选）
     *
     * @param year       年份（可为 null）
     * @param seasonType 季节类型（可为 null）
     * @param status     状态（可为 null）
     * @return 季节列表（不含波段详情）
     */
    public List<Season> listSeasons(Integer year, String seasonType, String status) {
        return seasonRepository.selectByCondition(year, seasonType, status);
    }

    /**
     * 查询季节详情（含波段列表）
     *
     * @param seasonId 季节ID
     * @return 季节实体（含 waves）
     */
    public Season getSeasonDetail(Long seasonId) {
        Season season = seasonRepository.selectById(seasonId);
        if (season == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "季节不存在");
        }

        // 填充波段列表
        List<Wave> waves = waveRepository.selectBySeasonId(seasonId);
        season.setWaves(waves);

        return season;
    }

    // ==================== 波段 CRUD ====================

    /**
     * 在季节下新增波段
     * <p>
     * 校验规则：
     * <ol>
     *   <li>季节必须存在</li>
     *   <li>季节必须为 ACTIVE 状态——已关闭的季节不允许新增波段</li>
     *   <li>同一季节内波段编码不可重复</li>
     *   <li>sortOrder 不传时自动追加到末尾</li>
     * </ol>
     * </p>
     *
     * @param seasonId 季节ID
     * @param wave     波段实体
     * @return 保存后的波段实体
     */
    public Wave createWave(Long seasonId, Wave wave) {
        // 校验季节存在
        Season season = seasonRepository.selectById(seasonId);
        if (season == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "季节不存在");
        }

        // 已关闭的季节不允许新增波段
        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "已关闭的季节不允许新增波段");
        }

        // 校验组内编码唯一性
        if (waveRepository.existsBySeasonIdAndCode(seasonId, wave.getCode(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "该季节下已存在相同编码的波段");
        }

        wave.setSeasonId(seasonId);

        // sortOrder 不传时自动追加到末尾
        if (wave.getSortOrder() == null) {
            int maxSortOrder = waveRepository.getMaxSortOrder(seasonId);
            wave.setSortOrder(maxSortOrder + 1);
        }

        try {
            waveRepository.insert(wave);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建波段触发唯一约束: seasonId={}, code={}", seasonId, wave.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "该季节下已存在相同编码的波段");
        }

        log.info("创建波段: seasonId={}, code={}, name={}, sortOrder={}",
                seasonId, wave.getCode(), wave.getName(), wave.getSortOrder());
        return wave;
    }

    /**
     * 更新波段
     * <p>
     * 可更新字段：name, deliveryDate, sortOrder。
     * 不允许变更编码（code）——波段编码被业务单据引用后修改会导致数据不一致。
     * 已关闭的季节下的波段不允许修改。
     * </p>
     *
     * @param waveId       波段ID
     * @param name         新名称（可为 null，不更新）
     * @param deliveryDate 新交货日期（可为 null，不更新）
     * @param sortOrder    新排序号（可为 null，不更新）
     * @return 更新后的波段
     */
    public Wave updateWave(Long waveId, String name,
                           java.time.LocalDate deliveryDate, Integer sortOrder) {
        Wave existing = waveRepository.selectById(waveId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "波段不存在");
        }

        // 检查所属季节是否已关闭
        Season season = seasonRepository.selectById(existing.getSeasonId());
        if (season != null && season.getStatus() == SeasonStatus.CLOSED) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "已关闭的季节下的波段不允许修改");
        }

        if (name != null) {
            existing.setName(name);
        }
        if (deliveryDate != null) {
            existing.setDeliveryDate(deliveryDate);
        }
        if (sortOrder != null) {
            existing.setSortOrder(sortOrder);
        }

        int rows = waveRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新波段: id={}", waveId);
        return waveRepository.selectById(waveId);
    }

    /**
     * 删除波段
     * <p>
     * 已关闭的季节下的波段不允许删除。
     * 波段被业务单据引用时也不允许删除（预留钩子）。
     * </p>
     *
     * @param waveId 波段ID
     */
    public void deleteWave(Long waveId) {
        Wave existing = waveRepository.selectById(waveId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "波段不存在");
        }

        // 检查所属季节是否已关闭
        Season season = seasonRepository.selectById(existing.getSeasonId());
        if (season != null && season.getStatus() == SeasonStatus.CLOSED) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "已关闭的季节下的波段不允许删除");
        }

        waveRepository.deleteById(waveId);
        log.info("删除波段: id={}, code={}, seasonId={}", waveId, existing.getCode(), existing.getSeasonId());
    }

    // ==================== 私有方法 ====================

    /**
     * 获取季节类型的中文名称
     *
     * @param seasonType 季节类型
     * @return 中文名称
     */
    private String getSeasonTypeName(SeasonType seasonType) {
        return seasonType == SeasonType.SPRING_SUMMER ? "春夏" : "秋冬";
    }

    /**
     * 统计引用该季节的订单数量
     * <p>
     * 当前订单模块尚未实现，返回 0。
     * 订单模块实现后，应替换为真实查询。
     * </p>
     *
     * @param seasonId 季节ID
     * @return 引用该季节的订单数量
     */
    private long countOrderReferences(Long seasonId) {
        // TODO: 订单模块实现后，注入订单 Mapper 并查询真实引用数量
        return 0;
    }
}
