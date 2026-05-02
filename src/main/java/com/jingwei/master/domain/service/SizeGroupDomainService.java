package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Size;
import com.jingwei.master.domain.model.SizeCategory;
import com.jingwei.master.domain.model.SizeGroup;
import com.jingwei.master.domain.repository.SizeGroupRepository;
import com.jingwei.master.domain.repository.SizeRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 尺码组领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>尺码组 CRUD 及业务校验（编码唯一、引用保护、状态控制）</li>
 *   <li>尺码组内尺码的增删改排序</li>
 *   <li>尺码组与 SPU 的引用保护（删除/修改编码时校验）</li>
 * </ul>
 * </p>
 * <p>
 * 关键业务规则：
 * <ul>
 *   <li>尺码组被 SPU 引用后不可删除，但可停用——停用后不再出现在创建 SPU 的选择列表中</li>
 *   <li>已被引用的尺码组可以新增尺码（追加到末尾），不影响已有 SKU</li>
 *   <li>已被引用的尺码组不可删除已有尺码或修改已有尺码的编码——会破坏已有 SKU 的编码一致性</li>
 * </ul>
 * </p>
 * <p>
 * 设计决策：
 * <ul>
 *   <li>isReferencedBySpu() 通过 SpuRepository 查询真实 SPU 引用数量，
 *       用于删除尺码组和修改/删除尺码时的引用保护校验</li>
 *   <li>尺码排序号由前端控制，后端只保证按 sort_order 排序返回</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SizeGroupDomainService {

    private final SizeGroupRepository sizeGroupRepository;
    private final SizeRepository sizeRepository;
    private final SpuRepository spuRepository;

    // ==================== 尺码组 CRUD ====================

    /**
     * 创建尺码组
     * <p>
     * 校验规则：
     * <ol>
     *   <li>尺码组编码全局唯一</li>
     *   <li>适用品类必须为 WOMEN/MEN/CHILDREN 之一</li>
     * </ol>
     * </p>
     *
     * @param sizeGroup 尺码组实体
     * @return 保存后的尺码组实体
     */
    public SizeGroup createSizeGroup(SizeGroup sizeGroup) {
        // 校验编码唯一性
        if (sizeGroupRepository.existsByCode(sizeGroup.getCode(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "尺码组编码已存在");
        }

        sizeGroup.setStatus(CommonStatus.ACTIVE);
        try {
            sizeGroupRepository.insert(sizeGroup);
        } catch (DuplicateKeyException e) {
            // 并发场景下数据库唯一索引兜底
            log.warn("并发创建尺码组触发唯一约束: code={}", sizeGroup.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "尺码组编码已存在");
        }

        log.info("创建尺码组: code={}, name={}, category={}, id={}",
                sizeGroup.getCode(), sizeGroup.getName(), sizeGroup.getCategory(), sizeGroup.getId());
        return sizeGroup;
    }

    /**
     * 更新尺码组
     * <p>
     * 可更新字段：name, category, status。
     * 不允许变更编码（code）——编码是尺码组的唯一标识，被 SPU 引用后修改会导致数据不一致。
     * </p>
     *
     * @param sizeGroupId 尺码组ID
     * @param name        新名称（可为 null，不更新）
     * @param category    新品类（可为 null，不更新）
     * @param status      新状态（可为 null，不更新）
     * @return 更新后的尺码组
     */
    public SizeGroup updateSizeGroup(Long sizeGroupId, String name, String category, String status) {
        SizeGroup existing = sizeGroupRepository.selectById(sizeGroupId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尺码组不存在");
        }

        if (name != null) {
            existing.setName(name);
        }
        if (category != null) {
            existing.setCategory(SizeCategory.valueOf(category));
        }
        if (status != null) {
            existing.setStatus(CommonStatus.valueOf(status));
        }

        int rows = sizeGroupRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新尺码组: id={}", sizeGroupId);
        return sizeGroupRepository.selectById(sizeGroupId);
    }

    /**
     * 删除尺码组
     * <p>
     * 校验规则：
     * <ol>
     *   <li>尺码组被 SPU 引用时不可删除——提示被引用数量</li>
     *   <li>删除尺码组时，同时逻辑删除组内所有尺码</li>
     * </ol>
     * </p>
     *
     * @param sizeGroupId 尺码组ID
     */
    public void deleteSizeGroup(Long sizeGroupId) {
        SizeGroup existing = sizeGroupRepository.selectById(sizeGroupId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尺码组不存在");
        }

        // 检查是否被 SPU 引用
        long spuCount = countSpuReferences(sizeGroupId);
        if (spuCount > 0) {
            throw new BizException(ErrorCode.SIZE_GROUP_REFERENCED,
                    "该尺码组已被" + spuCount + "个款式引用，不可删除");
        }

        // 先删除组内所有尺码
        List<Size> sizes = sizeRepository.selectBySizeGroupId(sizeGroupId);
        for (Size size : sizes) {
            sizeRepository.deleteById(size.getId());
        }

        // 再删除尺码组
        sizeGroupRepository.deleteById(sizeGroupId);
        log.info("删除尺码组及组内{}个尺码: id={}, code={}", sizes.size(), sizeGroupId, existing.getCode());
    }

    /**
     * 查询尺码组列表（支持按品类和状态筛选）
     *
     * @param category 适用品类（可为 null）
     * @param status   状态（可为 null）
     * @return 尺码组列表（不含尺码详情）
     */
    public List<SizeGroup> listSizeGroups(String category, String status) {
        return sizeGroupRepository.selectByCondition(category, status);
    }

    /**
     * 查询尺码组详情（含尺码列表）
     *
     * @param sizeGroupId 尺码组ID
     * @return 尺码组实体（含 sizes）
     */
    public SizeGroup getSizeGroupDetail(Long sizeGroupId) {
        SizeGroup sizeGroup = sizeGroupRepository.selectById(sizeGroupId);
        if (sizeGroup == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尺码组不存在");
        }

        // 填充尺码列表
        List<Size> sizes = sizeRepository.selectBySizeGroupId(sizeGroupId);
        sizeGroup.setSizes(sizes);

        return sizeGroup;
    }

    // ==================== 尺码 CRUD ====================

    /**
     * 在尺码组下新增尺码
     * <p>
     * 校验规则：
     * <ol>
     *   <li>尺码组必须存在</li>
     *   <li>同一尺码组内编码不可重复</li>
     *   <li>sortOrder 不传时自动追加到末尾</li>
     * </ol>
     * </p>
     * <p>
     * 已被 SPU 引用的尺码组也允许新增尺码（追加到末尾），这不会影响已有 SKU 数据，
     * 只会在后续创建新的 Color-way 时多出对应的 SKU。
     * </p>
     *
     * @param sizeGroupId 尺码组ID
     * @param size        尺码实体
     * @return 保存后的尺码实体
     */
    public Size createSize(Long sizeGroupId, Size size) {
        // 校验尺码组存在
        SizeGroup sizeGroup = sizeGroupRepository.selectById(sizeGroupId);
        if (sizeGroup == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尺码组不存在");
        }

        // 校验组内编码唯一性
        if (sizeRepository.existsBySizeGroupIdAndCode(sizeGroupId, size.getCode(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "该尺码组内已存在相同编码的尺码");
        }

        size.setSizeGroupId(sizeGroupId);

        // sortOrder 不传时自动追加到末尾
        if (size.getSortOrder() == null) {
            int maxSortOrder = sizeRepository.getMaxSortOrder(sizeGroupId);
            size.setSortOrder(maxSortOrder + 1);
        }

        try {
            sizeRepository.insert(size);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建尺码触发唯一约束: sizeGroupId={}, code={}", sizeGroupId, size.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "该尺码组内已存在相同编码的尺码");
        }

        log.info("创建尺码: sizeGroupId={}, code={}, name={}, sortOrder={}",
                sizeGroupId, size.getCode(), size.getName(), size.getSortOrder());
        return size;
    }

    /**
     * 更新尺码
     * <p>
     * 可更新字段：name, sortOrder。
     * 编码（code）修改受 SPU 引用保护：如果尺码组已被 SPU 引用，不允许修改编码。
     * </p>
     *
     * @param sizeId   尺码ID
     * @param code     新编码（可为 null，不更新）
     * @param name     新名称（可为 null，不更新）
     * @param sortOrder 新排序号（可为 null，不更新）
     * @return 更新后的尺码
     */
    public Size updateSize(Long sizeId, String code, String name, Integer sortOrder) {
        Size existing = sizeRepository.selectById(sizeId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尺码不存在");
        }

        // 修改编码时校验 SPU 引用保护
        if (code != null && !code.equals(existing.getCode())) {
            long spuCount = countSpuReferences(existing.getSizeGroupId());
            if (spuCount > 0) {
                throw new BizException(ErrorCode.SIZE_GROUP_REFERENCED,
                        "该尺码组已被" + spuCount + "个款式引用，不可修改已有尺码的编码");
            }

            // 校验组内新编码唯一性
            if (sizeRepository.existsBySizeGroupIdAndCode(existing.getSizeGroupId(), code, sizeId)) {
                throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "该尺码组内已存在相同编码的尺码");
            }

            existing.setCode(code);
        }

        if (name != null) {
            existing.setName(name);
        }
        if (sortOrder != null) {
            existing.setSortOrder(sortOrder);
        }

        int rows = sizeRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新尺码: id={}", sizeId);
        return sizeRepository.selectById(sizeId);
    }

    /**
     * 删除尺码
     * <p>
     * 校验规则：
     * <ol>
     *   <li>如果尺码组已被 SPU 引用，不可删除尺码——删除尺码会影响已有 SKU 的编码和矩阵结构</li>
     *   <li>未被引用的尺码组，可以自由删除组内尺码</li>
     * </ol>
     * </p>
     *
     * @param sizeId 尺码ID
     */
    public void deleteSize(Long sizeId) {
        Size existing = sizeRepository.selectById(sizeId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尺码不存在");
        }

        // 检查尺码组是否被 SPU 引用
        long spuCount = countSpuReferences(existing.getSizeGroupId());
        if (spuCount > 0) {
            throw new BizException(ErrorCode.SIZE_GROUP_REFERENCED,
                    "该尺码组已被" + spuCount + "个款式引用，不可删除尺码");
        }

        sizeRepository.deleteById(sizeId);
        log.info("删除尺码: id={}, code={}, sizeGroupId={}", sizeId, existing.getCode(), existing.getSizeGroupId());
    }

    // ==================== SPU 引用检查 ====================

    /**
     * 统计引用该尺码组的 SPU 数量
     * <p>
     * 通过注入 SpuRepository 查询引用该尺码组的 SPU 数量，
     * 用于删除尺码组和修改/删除尺码时的引用保护校验。
     * </p>
     *
     * @param sizeGroupId 尺码组ID
     * @return 引用该尺码组的 SPU 数量
     */
    private long countSpuReferences(Long sizeGroupId) {
        return spuRepository.countBySizeGroupId(sizeGroupId);
    }
}
