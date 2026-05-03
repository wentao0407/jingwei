package com.jingwei.approval.interfaces.controller;

import com.jingwei.approval.application.dto.ApproveDTO;
import com.jingwei.approval.application.dto.CreateApprovalConfigDTO;
import com.jingwei.approval.application.dto.SubmitApprovalDTO;
import com.jingwei.approval.application.dto.UpdateApprovalConfigDTO;
import com.jingwei.approval.application.service.ApprovalApplicationService;
import com.jingwei.approval.interfaces.vo.ApprovalConfigVO;
import com.jingwei.approval.interfaces.vo.ApprovalTaskVO;
import com.jingwei.common.domain.model.R;
import com.jingwei.common.config.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批管理 Controller
 * <p>
 * 提供审批配置 CRUD、审批操作、审批查询接口。
 * 所有接口统一使用 POST 方法（除文件上传外）。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalApplicationService approvalApplicationService;

    // ========== 审批配置管理 ==========

    /**
     * 创建审批配置
     */
    @PostMapping("/config/create")
    @RequirePermission("system:approvalConfig:create")
    public R<ApprovalConfigVO> createConfig(@Valid @RequestBody CreateApprovalConfigDTO dto) {
        return R.ok(approvalApplicationService.createConfig(dto));
    }

    /**
     * 更新审批配置
     */
    @PostMapping("/config/update")
    @RequirePermission("system:approvalConfig:update")
    public R<ApprovalConfigVO> updateConfig(@Valid @RequestBody UpdateApprovalConfigDTO dto) {
        return R.ok(approvalApplicationService.updateConfig(dto));
    }

    /**
     * 删除审批配置
     */
    @PostMapping("/config/delete")
    @RequirePermission("system:approvalConfig:delete")
    public R<Void> deleteConfig(@RequestBody Long configId) {
        approvalApplicationService.deleteConfig(configId);
        return R.ok(null);
    }

    /**
     * 查询审批配置详情
     */
    @PostMapping("/config/detail")
    public R<ApprovalConfigVO> getConfig(@RequestBody Long configId) {
        return R.ok(approvalApplicationService.getConfig(configId));
    }

    /**
     * 查询所有审批配置列表
     */
    @PostMapping("/config/list")
    public R<List<ApprovalConfigVO>> listConfigs() {
        return R.ok(approvalApplicationService.listAllConfigs());
    }

    // ========== 审批操作 ==========

    /**
     * 提交审批
     * <p>
     * 由业务模块（如销售订单）调用，将业务单据提交到审批引擎。
     * 返回 true 表示需要人工审批，false 表示自动通过。
     * </p>
     */
    @PostMapping("/submit")
    public R<Boolean> submitForApproval(@Valid @RequestBody SubmitApprovalDTO dto) {
        return R.ok(approvalApplicationService.submitForApproval(dto));
    }

    /**
     * 审批操作（通过或驳回）
     */
    @PostMapping("/approve")
    public R<Void> approve(@Valid @RequestBody ApproveDTO dto) {
        approvalApplicationService.approve(dto);
        return R.ok(null);
    }

    // ========== 审批查询 ==========

    /**
     * 查询当前用户的待办审批列表
     */
    @PostMapping("/task/myPending")
    public R<List<ApprovalTaskVO>> listMyPendingTasks() {
        return R.ok(approvalApplicationService.listMyPendingTasks());
    }

    /**
     * 查询指定业务单据的审批记录
     */
    @PostMapping("/task/records")
    public R<List<ApprovalTaskVO>> listApprovalRecords(@RequestBody SubmitApprovalDTO dto) {
        return R.ok(approvalApplicationService.listApprovalRecords(
                dto.getBusinessType(), dto.getBusinessId()));
    }
}
