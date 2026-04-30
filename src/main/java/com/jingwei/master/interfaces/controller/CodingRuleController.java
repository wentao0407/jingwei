package com.jingwei.master.interfaces.controller;

import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.CreateCodingRuleDTO;
import com.jingwei.master.application.dto.GenerateCodeDTO;
import com.jingwei.master.application.dto.UpdateCodingRuleDTO;
import com.jingwei.master.application.service.CodingRuleApplicationService;
import com.jingwei.master.interfaces.vo.CodePreviewVO;
import com.jingwei.master.interfaces.vo.CodingRuleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 编码规则管理 Controller
 * <p>
 * 提供编码规则 CRUD、编码生成、编码预览接口。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CodingRuleController {

    private final CodingRuleApplicationService codingRuleApplicationService;

    /**
     * 创建编码规则
     */
    @PostMapping("/master/codingRule/create")
    public R<CodingRuleVO> createRule(@Valid @RequestBody CreateCodingRuleDTO dto) {
        return R.ok(codingRuleApplicationService.createRule(dto));
    }

    /**
     * 更新编码规则
     */
    @PostMapping("/master/codingRule/update")
    public R<CodingRuleVO> updateRule(@RequestParam Long ruleId, @Valid @RequestBody UpdateCodingRuleDTO dto) {
        return R.ok(codingRuleApplicationService.updateRule(ruleId, dto));
    }

    /**
     * 删除编码规则（已使用的规则不可删除）
     */
    @PostMapping("/master/codingRule/delete")
    public R<Void> deleteRule(@RequestParam Long ruleId) {
        codingRuleApplicationService.deleteRule(ruleId);
        return R.ok();
    }

    /**
     * 查询所有编码规则
     */
    @PostMapping("/master/codingRule/list")
    public R<List<CodingRuleVO>> listAllRules() {
        return R.ok(codingRuleApplicationService.listAllRules());
    }

    /**
     * 根据编码查询规则详情
     */
    @PostMapping("/master/codingRule/detail")
    public R<CodingRuleVO> getRuleByCode(@RequestParam String code) {
        return R.ok(codingRuleApplicationService.getRuleByCode(code));
    }

    /**
     * 生成编码（原子递增流水号）
     */
    @PostMapping("/master/codingRule/generate")
    public R<String> generateCode(@Valid @RequestBody GenerateCodeDTO dto) {
        return R.ok(codingRuleApplicationService.generateCode(dto.getRuleCode(), dto.getContext()));
    }

    /**
     * 预览编码（不递增流水号）
     */
    @PostMapping("/master/codingRule/preview")
    public R<CodePreviewVO> previewCode(@Valid @RequestBody GenerateCodeDTO dto) {
        return R.ok(codingRuleApplicationService.previewCode(dto.getRuleCode(), dto.getContext()));
    }
}
