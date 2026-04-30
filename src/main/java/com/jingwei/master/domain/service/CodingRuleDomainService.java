package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.application.dto.CodingRuleSegmentDTO;
import com.jingwei.master.application.dto.UpdateCodingRuleDTO;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.CodingRuleRepository;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.master.domain.repository.CodingRuleSegmentRepository;
import com.jingwei.master.domain.repository.CodingSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 编码规则领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>编码规则 CRUD 及业务校验</li>
 *   <li>编码生成核心逻辑（按段类型拼接）</li>
 *   <li>预览功能（不递增流水号）</li>
 * </ul>
 * </p>
 * <p>
 * 编码生成流程：
 * 1. 查询规则及段列表（按 sort_order 排序）
 * 2. 逐段拼接：FIXED→固定文本，DATE→日期格式化，SEQUENCE→流水号原子递增，
 *    SEASON/WAREHOUSE/CUSTOM→从上下文获取
 * 3. 流水号递增使用数据库行级锁（SELECT ... FOR UPDATE）保证并发安全
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodingRuleDomainService {

    private final CodingRuleRepository codingRuleRepository;
    private final CodingRuleSegmentRepository codingRuleSegmentRepository;
    private final CodingSequenceRepository codingSequenceRepository;

    // ==================== 编码规则 CRUD ====================

    /**
     * 创建编码规则
     * <p>
     * 校验：规则编码唯一性。
     * </p>
     *
     * @param rule     规则实体
     * @param segments 段列表
     * @return 保存后的规则实体
     */
    public CodingRule createRule(CodingRule rule, List<CodingRuleSegment> segments) {
        // 校验规则编码唯一性
        if (codingRuleRepository.existsByCode(rule.getCode())) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "规则编码已存在");
        }

        // 校验段列表：必须包含且仅包含一个 SEQUENCE 段
        validateSegments(segments);

        rule.setStatus(UserStatus.ACTIVE);
        rule.setUsed(false);
        codingRuleRepository.insert(rule);

        // 批量插入段
        for (CodingRuleSegment segment : segments) {
            segment.setRuleId(rule.getId());
        }
        codingRuleSegmentRepository.batchInsert(segments);

        log.info("创建编码规则: code={}, id={}", rule.getCode(), rule.getId());
        return rule;
    }

    /**
     * 更新编码规则基本信息（不含段列表）
     *
     * @param ruleId 规则ID
     * @param dto    更新请求
     * @return 更新后的规则
     */
    public CodingRule updateRule(Long ruleId, UpdateCodingRuleDTO dto) {
        CodingRule existing = codingRuleRepository.selectById(ruleId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "编码规则不存在");
        }

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getBusinessType() != null) {
            existing.setBusinessType(dto.getBusinessType());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(UserStatus.valueOf(dto.getStatus()));
        }

        int rows = codingRuleRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新编码规则: id={}", ruleId);
        return codingRuleRepository.selectById(ruleId);
    }

    /**
     * 删除编码规则
     * <p>
     * 已使用的规则不可删除，只能停用。
     * </p>
     *
     * @param ruleId 规则ID
     */
    public void deleteRule(Long ruleId) {
        CodingRule existing = codingRuleRepository.selectById(ruleId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "编码规则不存在");
        }

        if (Boolean.TRUE.equals(existing.getUsed())) {
            throw new BizException(ErrorCode.CODING_RULE_USED);
        }

        // 先删段，再删规则
        codingRuleSegmentRepository.deleteByRuleId(ruleId);
        codingRuleRepository.deleteById(ruleId);

        log.info("删除编码规则: id={}, code={}", ruleId, existing.getCode());
    }

    /**
     * 标记规则为已使用
     * <p>
     * 生成编码后调用，防止已使用的规则被删除。
     * </p>
     *
     * @param ruleId 规则ID
     */
    public void markAsUsed(Long ruleId) {
        CodingRule rule = codingRuleRepository.selectById(ruleId);
        if (rule != null && !Boolean.TRUE.equals(rule.getUsed())) {
            rule.setUsed(true);
            codingRuleRepository.updateById(rule);
        }
    }

    /**
     * 查询所有规则
     */
    public List<CodingRule> listAllRules() {
        return codingRuleRepository.selectAll();
    }

    /**
     * 根据编码查询规则
     */
    public CodingRule getRuleByCode(String code) {
        CodingRule rule = codingRuleRepository.selectByCode(code);
        if (rule == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "编码规则不存在: " + code);
        }
        return rule;
    }

    /**
     * 查询规则的段列表
     */
    public List<CodingRuleSegment> getSegments(Long ruleId) {
        return codingRuleSegmentRepository.selectByRuleId(ruleId);
    }

    // ==================== 编码生成核心逻辑 ====================

    /**
     * 生成编码
     * <p>
     * 按段类型拼接，SEQUENCE 段原子递增流水号。
     * 此方法必须在事务中调用（因为包含 SELECT ... FOR UPDATE）。
     * </p>
     *
     * @param ruleCode 规则编码
     * @param context  上下文变量（仓库编码、季节编码等）
     * @return 生成的编码字符串
     */
    public String generateCode(String ruleCode, Map<String, String> context) {
        // 1. 查询规则
        CodingRule rule = getRuleByCode(ruleCode);

        // 检查规则状态
        if (rule.getStatus() == UserStatus.INACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "编码规则已停用");
        }

        // 2. 查询段列表（已按 sort_order 排序）
        List<CodingRuleSegment> segments = codingRuleSegmentRepository.selectByRuleId(rule.getId());
        if (segments.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "编码规则无段配置");
        }

        // 3. 逐段拼接
        StringBuilder code = new StringBuilder();
        for (CodingRuleSegment seg : segments) {
            // 拼接连接符
            code.append(seg.getConnector());

            switch (seg.getSegmentType()) {
                case FIXED -> code.append(seg.getSegmentValue());
                case DATE -> code.append(renderDateSegment(seg.getSegmentValue()));
                case SEQUENCE -> code.append(renderSequenceSegment(rule.getId(), seg));
                case SEASON -> code.append(getContextValue(context, "seasonCode", "SEASON"));
                case WAREHOUSE -> code.append(getContextValue(context, "warehouseCode", "WAREHOUSE"));
                case CUSTOM -> code.append(getContextValue(context, seg.getSegmentValue(), "CUSTOM:" + seg.getSegmentValue()));
            }
        }

        // 4. 标记规则为已使用
        markAsUsed(rule.getId());

        String generatedCode = code.toString();
        log.info("生成编码: ruleCode={}, code={}", ruleCode, generatedCode);
        return generatedCode;
    }

    /**
     * 预览编码（不递增流水号，只展示生成效果）
     * <p>
     * 流水号使用示例值（如 00001），不写入数据库。
     * </p>
     *
     * @param ruleCode 规则编码
     * @param context  上下文变量
     * @return 预览编码
     */
    public String previewCode(String ruleCode, Map<String, String> context) {
        CodingRule rule = getRuleByCode(ruleCode);
        List<CodingRuleSegment> segments = codingRuleSegmentRepository.selectByRuleId(rule.getId());
        if (segments.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "编码规则无段配置");
        }

        StringBuilder code = new StringBuilder();
        for (CodingRuleSegment seg : segments) {
            code.append(seg.getConnector());

            switch (seg.getSegmentType()) {
                case FIXED -> code.append(seg.getSegmentValue());
                case DATE -> code.append(renderDateSegment(seg.getSegmentValue()));
                case SEQUENCE -> {
                    // 预览时不递增，使用示例值：1 补零到指定位数
                    int length = seg.getSeqLength() != null ? seg.getSeqLength() : 4;
                    code.append(String.format("%0" + length + "d", 1));
                }
                case SEASON -> code.append(getContextValue(context, "seasonCode", "SEASON"));
                case WAREHOUSE -> code.append(getContextValue(context, "warehouseCode", "WAREHOUSE"));
                case CUSTOM -> code.append(getContextValue(context, seg.getSegmentValue(), "CUSTOM:" + seg.getSegmentValue()));
            }
        }

        return code.toString();
    }

    // ==================== 私有方法 ====================

    /**
     * 渲染 DATE 段：按 segmentValue 指定的格式渲染当前日期
     *
     * @param pattern 日期格式（如 YYYYMM、YYYYMMDD）
     * @return 格式化后的日期字符串
     */
    private String renderDateSegment(String pattern) {
        try {
            // Java 的 DateTimeFormatter 使用 yyyy 而不是 YYYY
            String javaPattern = pattern.replace("YYYY", "yyyy");
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(javaPattern));
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "日期格式无效: " + pattern);
        }
    }

    /**
     * 渲染 SEQUENCE 段：原子递增流水号
     * <p>
     * 根据 seqResetType 计算重置键，调用 codingSequenceRepository.incrementAndGet
     * 使用 SELECT ... FOR UPDATE 行级锁保证原子递增。
     * </p>
     *
     * @param ruleId 规则ID
     * @param seg    段配置
     * @return 补零后的流水号字符串
     */
    private String renderSequenceSegment(Long ruleId, CodingRuleSegment seg) {
        int length = seg.getSeqLength() != null ? seg.getSeqLength() : 4;
        SeqResetType resetType = seg.getSeqResetType() != null ? seg.getSeqResetType() : SeqResetType.NEVER;

        // 计算重置键
        String resetKey = buildResetKey(resetType);

        // 原子递增（含 FOR UPDATE 行级锁）
        long nextVal = codingSequenceRepository.incrementAndGet(ruleId, resetKey, length);

        // 补零到指定位数
        return String.format("%0" + length + "d", nextVal);
    }

    /**
     * 根据重置方式计算重置键
     * <p>
     * NEVER → ""（空字符串，全局递增）
     * YEARLY → "2026"（年份）
     * MONTHLY → "202604"（年月）
     * DAILY → "20260430"（年月日）
     * </p>
     *
     * @param resetType 重置方式
     * @return 重置键
     */
    private String buildResetKey(SeqResetType resetType) {
        LocalDateTime now = LocalDateTime.now();
        return switch (resetType) {
            case NEVER -> "";
            case YEARLY -> String.valueOf(now.getYear());
            case MONTHLY -> String.format("%d%02d", now.getYear(), now.getMonthValue());
            case DAILY -> String.format("%d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        };
    }

    /**
     * 从上下文获取值
     *
     * @param context  上下文 Map
     * @param key      键
     * @param label    键的标签（用于错误提示）
     * @return 值
     */
    private String getContextValue(Map<String, String> context, String key, String label) {
        if (context == null || !context.containsKey(key)) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "生成编码缺少上下文参数: " + label);
        }
        return context.get(key);
    }

    /**
     * 校验段列表
     * <p>
     * 规则：必须包含且仅包含一个 SEQUENCE 段。
     * </p>
     */
    private void validateSegments(List<CodingRuleSegment> segments) {
        long seqCount = segments.stream()
                .filter(s -> s.getSegmentType() == SegmentType.SEQUENCE)
                .count();
        if (seqCount == 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "编码规则必须包含一个流水号段");
        }
        if (seqCount > 1) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "编码规则只能包含一个流水号段");
        }
    }
}
