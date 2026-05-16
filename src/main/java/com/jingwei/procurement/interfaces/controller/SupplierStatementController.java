package com.jingwei.procurement.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.procurement.application.dto.GenerateStatementDTO;
import com.jingwei.procurement.application.dto.StatementQueryDTO;
import com.jingwei.procurement.application.service.SupplierStatementApplicationService;
import com.jingwei.procurement.interfaces.vo.StatementVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商对账单 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class SupplierStatementController {

    private final SupplierStatementApplicationService statementApplicationService;

    /**
     * 生成对账单
     */
    @RequirePermission("procurement:statement:generate")
    @PostMapping("/procurement/statement/generate")
    public R<StatementVO> generateStatement(@Valid @RequestBody GenerateStatementDTO dto) {
        return R.ok(statementApplicationService.generateStatement(dto));
    }

    /**
     * 确认对账单
     */
    @RequirePermission("procurement:statement:confirm")
    @PostMapping("/procurement/statement/confirm")
    public R<Void> confirmStatement(@RequestParam Long statementId) {
        statementApplicationService.confirmStatement(statementId);
        return R.ok();
    }

    /**
     * 标记争议
     */
    @RequirePermission("procurement:statement:dispute")
    @PostMapping("/procurement/statement/dispute")
    public R<Void> disputeStatement(@RequestParam Long statementId) {
        statementApplicationService.disputeStatement(statementId);
        return R.ok();
    }

    /**
     * 查询对账单详情
     */
    @PostMapping("/procurement/statement/detail")
    public R<StatementVO> getDetail(@RequestParam Long statementId) {
        return R.ok(statementApplicationService.getDetail(statementId));
    }

    /**
     * 分页查询对账单
     */
    @PostMapping("/procurement/statement/page")
    public R<IPage<StatementVO>> pageQuery(@Valid @RequestBody StatementQueryDTO dto) {
        return R.ok(statementApplicationService.pageQuery(dto));
    }
}
