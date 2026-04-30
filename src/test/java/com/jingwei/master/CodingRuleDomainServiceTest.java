package com.jingwei.master;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.application.dto.UpdateCodingRuleDTO;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.CodingRuleRepository;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.master.domain.repository.CodingRuleSegmentRepository;
import com.jingwei.master.domain.repository.CodingSequenceRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CodingRuleDomainService 单元测试
 * <p>
 * 测试编码规则领域服务的核心业务规则：
 * <ul>
 *   <li>规则编码唯一性校验</li>
 *   <li>段列表校验（必须包含一个 SEQUENCE 段）</li>
 *   <li>已使用的规则不可删除</li>
 *   <li>编码生成逻辑：FIXED/DATE/SEQUENCE/SEASON/WAREHOUSE/CUSTOM</li>
 *   <li>预览功能不递增流水号</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class CodingRuleDomainServiceTest {

    @Mock
    private CodingRuleRepository codingRuleRepository;

    @Mock
    private CodingRuleSegmentRepository codingRuleSegmentRepository;

    @Mock
    private CodingSequenceRepository codingSequenceRepository;

    @InjectMocks
    private CodingRuleDomainService codingRuleDomainService;

    // ==================== 创建规则 ====================

    @Test
    @DisplayName("创建规则 — 编码重复应抛异常")
    void createRule_duplicateCode_shouldThrow() {
        CodingRule rule = new CodingRule();
        rule.setCode("SALES_ORDER");
        when(codingRuleRepository.existsByCode("SALES_ORDER")).thenReturn(true);

        BizException ex = assertThrows(BizException.class,
                () -> codingRuleDomainService.createRule(rule, List.of(buildSeqSegment())));
        assertEquals(ErrorCode.DATA_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建规则 — 无 SEQUENCE 段应抛异常")
    void createRule_noSequenceSegment_shouldThrow() {
        CodingRule rule = new CodingRule();
        rule.setCode("NEW_RULE");
        when(codingRuleRepository.existsByCode("NEW_RULE")).thenReturn(false);

        CodingRuleSegment fixedSeg = new CodingRuleSegment();
        fixedSeg.setSegmentType(SegmentType.FIXED);
        fixedSeg.setSegmentValue("XX");
        fixedSeg.setSortOrder(1);

        BizException ex = assertThrows(BizException.class,
                () -> codingRuleDomainService.createRule(rule, List.of(fixedSeg)));
        assertTrue(ex.getMessage().contains("流水号段"));
    }

    @Test
    @DisplayName("创建规则 — 多个 SEQUENCE 段应抛异常")
    void createRule_multipleSequenceSegments_shouldThrow() {
        CodingRule rule = new CodingRule();
        rule.setCode("NEW_RULE");
        when(codingRuleRepository.existsByCode("NEW_RULE")).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> codingRuleDomainService.createRule(rule, List.of(buildSeqSegment(), buildSeqSegment())));
        assertTrue(ex.getMessage().contains("只能包含一个"));
    }

    @Test
    @DisplayName("创建规则 — 正常创建应成功")
    void createRule_shouldSucceed() {
        CodingRule rule = new CodingRule();
        rule.setCode("NEW_RULE");
        when(codingRuleRepository.existsByCode("NEW_RULE")).thenReturn(false);
        when(codingRuleRepository.insert(any())).thenReturn(1);
        when(codingRuleSegmentRepository.batchInsert(any())).thenReturn(1);

        CodingRuleSegment fixedSeg = new CodingRuleSegment();
        fixedSeg.setSegmentType(SegmentType.FIXED);
        fixedSeg.setSegmentValue("XX");
        fixedSeg.setSortOrder(1);

        List<CodingRuleSegment> segments = List.of(fixedSeg, buildSeqSegment());
        CodingRule result = codingRuleDomainService.createRule(rule, segments);

        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertFalse(result.getUsed());
    }

    // ==================== 删除规则 ====================

    @Test
    @DisplayName("删除规则 — 已使用应抛异常")
    void deleteRule_used_shouldThrow() {
        CodingRule existing = new CodingRule();
        existing.setId(1L);
        existing.setUsed(true);
        when(codingRuleRepository.selectById(1L)).thenReturn(existing);

        BizException ex = assertThrows(BizException.class,
                () -> codingRuleDomainService.deleteRule(1L));
        assertEquals(ErrorCode.CODING_RULE_USED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("删除规则 — 未使用应成功")
    void deleteRule_notUsed_shouldSucceed() {
        CodingRule existing = new CodingRule();
        existing.setId(1L);
        existing.setUsed(false);
        when(codingRuleRepository.selectById(1L)).thenReturn(existing);
        when(codingRuleSegmentRepository.deleteByRuleId(1L)).thenReturn(3);
        when(codingRuleRepository.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> codingRuleDomainService.deleteRule(1L));
        verify(codingRuleSegmentRepository).deleteByRuleId(1L);
        verify(codingRuleRepository).deleteById(1L);
    }

    // ==================== 编码生成 ====================

    @Test
    @DisplayName("生成编码 — FIXED+DATE+SEQUENCE 格式正确")
    void generateCode_fixedDateSequence_formatCorrect() {
        CodingRule rule = new CodingRule();
        rule.setId(1L);
        rule.setCode("SALES_ORDER");
        rule.setStatus(UserStatus.ACTIVE);
        when(codingRuleRepository.selectByCode("SALES_ORDER")).thenReturn(rule);

        CodingRuleSegment fixedSeg = new CodingRuleSegment();
        fixedSeg.setSegmentType(SegmentType.FIXED);
        fixedSeg.setSegmentValue("SO");
        fixedSeg.setConnector("");
        fixedSeg.setSortOrder(1);

        CodingRuleSegment dateSeg = new CodingRuleSegment();
        dateSeg.setSegmentType(SegmentType.DATE);
        dateSeg.setSegmentValue("YYYYMM");
        dateSeg.setConnector("-");
        dateSeg.setSortOrder(2);

        CodingRuleSegment seqSeg = new CodingRuleSegment();
        seqSeg.setSegmentType(SegmentType.SEQUENCE);
        seqSeg.setSeqLength(5);
        seqSeg.setSeqResetType(SeqResetType.MONTHLY);
        seqSeg.setConnector("-");
        seqSeg.setSortOrder(3);

        when(codingRuleSegmentRepository.selectByRuleId(1L))
                .thenReturn(List.of(fixedSeg, dateSeg, seqSeg));
        when(codingSequenceRepository.incrementAndGet(eq(1L), anyString(), eq(5))).thenReturn(1L);
        when(codingRuleRepository.selectById(1L)).thenReturn(rule);

        String code = codingRuleDomainService.generateCode("SALES_ORDER", Map.of());

        // 格式应为 SO-yyyyMM-00001
        assertTrue(code.startsWith("SO-"), "编码应以 SO- 开头: " + code);
        assertTrue(code.endsWith("-00001"), "编码应以 -00001 结尾: " + code);
    }

    @Test
    @DisplayName("生成编码 — WAREHOUSE/CUSTOM 段从上下文获取")
    void generateCode_warehouseCustom_fromContext() {
        CodingRule rule = new CodingRule();
        rule.setId(2L);
        rule.setCode("INBOUND_ORDER");
        rule.setStatus(UserStatus.ACTIVE);
        when(codingRuleRepository.selectByCode("INBOUND_ORDER")).thenReturn(rule);

        CodingRuleSegment fixedSeg = new CodingRuleSegment();
        fixedSeg.setSegmentType(SegmentType.FIXED);
        fixedSeg.setSegmentValue("RK");
        fixedSeg.setConnector("");
        fixedSeg.setSortOrder(1);

        CodingRuleSegment whSeg = new CodingRuleSegment();
        whSeg.setSegmentType(SegmentType.WAREHOUSE);
        whSeg.setConnector("-");
        whSeg.setSortOrder(2);

        CodingRuleSegment seqSeg = new CodingRuleSegment();
        seqSeg.setSegmentType(SegmentType.SEQUENCE);
        seqSeg.setSeqLength(4);
        seqSeg.setSeqResetType(SeqResetType.DAILY);
        seqSeg.setConnector("-");
        seqSeg.setSortOrder(3);

        when(codingRuleSegmentRepository.selectByRuleId(2L))
                .thenReturn(List.of(fixedSeg, whSeg, seqSeg));
        when(codingSequenceRepository.incrementAndGet(eq(2L), anyString(), eq(4))).thenReturn(1L);
        when(codingRuleRepository.selectById(2L)).thenReturn(rule);

        String code = codingRuleDomainService.generateCode("INBOUND_ORDER",
                Map.of("warehouseCode", "WH01"));

        assertTrue(code.startsWith("RK-WH01-"), "编码应以 RK-WH01- 开头: " + code);
        assertTrue(code.endsWith("-0001"), "编码应以 -0001 结尾: " + code);
    }

    @Test
    @DisplayName("生成编码 — 缺少上下文参数应抛异常")
    void generateCode_missingContext_shouldThrow() {
        CodingRule rule = new CodingRule();
        rule.setId(3L);
        rule.setCode("NEEDS_WH");
        rule.setStatus(UserStatus.ACTIVE);
        when(codingRuleRepository.selectByCode("NEEDS_WH")).thenReturn(rule);

        CodingRuleSegment whSeg = new CodingRuleSegment();
        whSeg.setSegmentType(SegmentType.WAREHOUSE);
        whSeg.setConnector("");
        whSeg.setSortOrder(1);

        when(codingRuleSegmentRepository.selectByRuleId(3L)).thenReturn(List.of(whSeg));

        BizException ex = assertThrows(BizException.class,
                () -> codingRuleDomainService.generateCode("NEEDS_WH", Map.of()));
        assertTrue(ex.getMessage().contains("WAREHOUSE"));
    }

    // ==================== 预览 ====================

    @Test
    @DisplayName("预览编码 — 不递增流水号")
    void previewCode_shouldNotIncrement() {
        CodingRule rule = new CodingRule();
        rule.setId(1L);
        rule.setCode("SALES_ORDER");
        rule.setStatus(UserStatus.ACTIVE);
        when(codingRuleRepository.selectByCode("SALES_ORDER")).thenReturn(rule);

        CodingRuleSegment fixedSeg = new CodingRuleSegment();
        fixedSeg.setSegmentType(SegmentType.FIXED);
        fixedSeg.setSegmentValue("SO");
        fixedSeg.setConnector("");
        fixedSeg.setSortOrder(1);

        CodingRuleSegment seqSeg = new CodingRuleSegment();
        seqSeg.setSegmentType(SegmentType.SEQUENCE);
        seqSeg.setSeqLength(4);
        seqSeg.setSeqResetType(SeqResetType.NEVER);
        seqSeg.setConnector("-");
        seqSeg.setSortOrder(2);

        when(codingRuleSegmentRepository.selectByRuleId(1L))
                .thenReturn(List.of(fixedSeg, seqSeg));

        String preview = codingRuleDomainService.previewCode("SALES_ORDER", Map.of());

        assertEquals("SO-0001", preview);
        // 验证没有调用 incrementAndGet
        verify(codingSequenceRepository, never()).incrementAndGet(anyLong(), anyString(), anyInt());
    }

    // ==================== 辅助方法 ====================

    private CodingRuleSegment buildSeqSegment() {
        CodingRuleSegment seg = new CodingRuleSegment();
        seg.setSegmentType(SegmentType.SEQUENCE);
        seg.setSeqLength(4);
        seg.setSeqResetType(SeqResetType.NEVER);
        seg.setSortOrder(2);
        return seg;
    }
}
