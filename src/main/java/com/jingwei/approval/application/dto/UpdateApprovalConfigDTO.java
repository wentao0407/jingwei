package com.jingwei.approval.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 更新审批配置 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateApprovalConfigDTO {

    /** 配置ID */
    @NotNull(message = "配置ID不能为空")
    private Long id;

    /** 配置名称 */
    private String configName;

    /** 审批模式：SINGLE / OR_SIGN */
    private String approvalMode;

    /** 审批人角色ID列表 */
    private List<Long> approverRoleIds;

    /** 是否启用 */
    private Boolean enabled;
}
