package com.jingwei.approval.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 审批操作 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ApproveDTO {

    /** 审批任务ID */
    @NotNull(message = "审批任务ID不能为空")
    private Long taskId;

    /** 是否通过 */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    /** 审批意见（必填） */
    @NotBlank(message = "审批意见不能为空")
    private String opinion;
}
