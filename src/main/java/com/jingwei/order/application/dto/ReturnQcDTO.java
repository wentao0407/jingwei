package com.jingwei.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 退货质检 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ReturnQcDTO {

    /** 退货单ID */
    @NotNull(message = "退货单ID不能为空")
    private Long returnId;

    /** 质检结果列表 */
    @NotEmpty(message = "质检结果不能为空")
    @Valid
    private List<QcLineResultDTO> results;

    /**
     * 单行质检结果
     */
    @Getter
    @Setter
    public static class QcLineResultDTO {

        /** 退货行ID */
        @NotNull(message = "退货行ID不能为空")
        private Long lineId;

        /** 合格数量 */
        @NotNull(message = "合格数量不能为空")
        private Integer passedQty;

        /** 不合格数量 */
        @NotNull(message = "不合格数量不能为空")
        private Integer failedQty;
    }
}
