package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 生成对账单 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class GenerateStatementDTO {

    /** 供应商ID */
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    /** 对账期间开始 */
    @NotNull(message = "开始日期不能为空")
    private LocalDate periodStart;

    /** 对账期间结束 */
    @NotNull(message = "结束日期不能为空")
    private LocalDate periodEnd;

    /** 备注 */
    private String remark;
}
