package com.jingwei.master.application.service;

import com.jingwei.master.application.dto.*;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.CodingRuleSegmentRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.master.interfaces.vo.CodePreviewVO;
import com.jingwei.master.interfaces.vo.CodingRuleSegmentVO;
import com.jingwei.master.interfaces.vo.CodingRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 编码规则应用服务
 * <p>
 * 负责编码规则的 CRUD 编排、编码生成的事务边界。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodingRuleApplicationService {

    private final CodingRuleDomainService codingRuleDomainService;
    private final CodingRuleSegmentRepository codingRuleSegmentRepository;

    /**
     * 创建编码规则
     */
    @Transactional(rollbackFor = Exception.class)
    public CodingRuleVO createRule(CreateCodingRuleDTO dto) {
        CodingRule rule = new CodingRule();
        rule.setCode(dto.getCode());
        rule.setName(dto.getName());
        rule.setBusinessType(dto.getBusinessType() != null ? dto.getBusinessType() : "");
        rule.setDescription(dto.getDescription() != null ? dto.getDescription() : "");

        // 转换段 DTO 为实体
        List<CodingRuleSegment> segments = new ArrayList<>();
        for (CodingRuleSegmentDTO segDto : dto.getSegments()) {
            CodingRuleSegment seg = new CodingRuleSegment();
            seg.setSegmentType(SegmentType.valueOf(segDto.getSegmentType()));
            seg.setSegmentValue(segDto.getSegmentValue() != null ? segDto.getSegmentValue() : "");
            seg.setSeqLength(segDto.getSeqLength() != null ? segDto.getSeqLength() : 0);
            seg.setSeqResetType(segDto.getSeqResetType() != null ? SeqResetType.valueOf(segDto.getSeqResetType()) : SeqResetType.NEVER);
            seg.setConnector(segDto.getConnector() != null ? segDto.getConnector() : "");
            seg.setSortOrder(segDto.getSortOrder());
            segments.add(seg);
        }

        codingRuleDomainService.createRule(rule, segments);
        return toCodingRuleVO(rule, segments);
    }

    /**
     * 更新编码规则基本信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CodingRuleVO updateRule(Long ruleId, UpdateCodingRuleDTO dto) {
        CodingRule updated = codingRuleDomainService.updateRule(ruleId, dto);
        List<CodingRuleSegment> segments = codingRuleSegmentRepository.selectByRuleId(ruleId);
        return toCodingRuleVO(updated, segments);
    }

    /**
     * 删除编码规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long ruleId) {
        codingRuleDomainService.deleteRule(ruleId);
    }

    /**
     * 查询所有编码规则
     */
    public List<CodingRuleVO> listAllRules() {
        List<CodingRule> rules = codingRuleDomainService.listAllRules();
        List<CodingRuleVO> result = new ArrayList<>();
        for (CodingRule rule : rules) {
            List<CodingRuleSegment> segments = codingRuleSegmentRepository.selectByRuleId(rule.getId());
            result.add(toCodingRuleVO(rule, segments));
        }
        return result;
    }

    /**
     * 根据编码查询规则详情
     */
    public CodingRuleVO getRuleByCode(String code) {
        CodingRule rule = codingRuleDomainService.getRuleByCode(code);
        List<CodingRuleSegment> segments = codingRuleSegmentRepository.selectByRuleId(rule.getId());
        return toCodingRuleVO(rule, segments);
    }

    /**
     * 生成编码
     * <p>
     * 必须在事务中执行（SEQUENCE 段使用 FOR UPDATE 行级锁）。
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public String generateCode(String ruleCode, Map<String, String> context) {
        return codingRuleDomainService.generateCode(ruleCode, context);
    }

    /**
     * 预览编码（不递增流水号）
     */
    public CodePreviewVO previewCode(String ruleCode, Map<String, String> context) {
        String preview = codingRuleDomainService.previewCode(ruleCode, context);
        CodePreviewVO vo = new CodePreviewVO();
        vo.setPreviewCode(preview);
        return vo;
    }

    // ==================== 转换方法 ====================

    private CodingRuleVO toCodingRuleVO(CodingRule rule, List<CodingRuleSegment> segments) {
        CodingRuleVO vo = new CodingRuleVO();
        vo.setId(rule.getId());
        vo.setCode(rule.getCode());
        vo.setName(rule.getName());
        vo.setBusinessType(rule.getBusinessType());
        vo.setDescription(rule.getDescription());
        vo.setStatus(rule.getStatus().name());
        vo.setUsed(rule.getUsed());
        vo.setCreatedAt(rule.getCreatedAt());
        vo.setUpdatedAt(rule.getUpdatedAt());

        List<CodingRuleSegmentVO> segVOs = segments.stream().map(seg -> {
            CodingRuleSegmentVO svo = new CodingRuleSegmentVO();
            svo.setId(seg.getId());
            svo.setSegmentType(seg.getSegmentType().name());
            svo.setSegmentValue(seg.getSegmentValue());
            svo.setSeqLength(seg.getSeqLength());
            svo.setSeqResetType(seg.getSeqResetType() != null ? seg.getSeqResetType().name() : "NEVER");
            svo.setConnector(seg.getConnector());
            svo.setSortOrder(seg.getSortOrder());
            return svo;
        }).toList();
        vo.setSegments(segVOs);
        return vo;
    }
}
