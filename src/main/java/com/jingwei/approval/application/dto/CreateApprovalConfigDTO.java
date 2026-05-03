package com.jingwei.approval.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建审批配置 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateApprovalConfigDTO {

    /** 业务类型（如 SALES_ORDER） */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /** 配置名称 */
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    /** 审批模式：SINGLE / OR_SIGN */
    @NotBlank(message = "审批模式不能为空")
    private String approvalMode;

    /** 审批人角色ID列表 */
    @NotNull(message = "审批角色不能为空")
    private List<Long> approverRoleIds;

    /** 是否启用 */
    private Boolean enabled = true;
}
