package com.jingwei.approval.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 提交审批 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class SubmitApprovalDTO {

    /** 业务类型（如 SALES_ORDER） */
    private String businessType;

    /** 业务单据ID */
    private Long businessId;

    /** 业务单据编号 */
    private String businessNo;
}
