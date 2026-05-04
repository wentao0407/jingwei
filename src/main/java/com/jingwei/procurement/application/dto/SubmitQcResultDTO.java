package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交检验结果 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class SubmitQcResultDTO {

    /** 到货行ID */
    @NotNull(message = "到货行ID不能为空")
    private Long lineId;

    /** 合格数量 */
    @NotNull(message = "合格数量不能为空")
    private BigDecimal acceptedQuantity;

    /** 不合格数量 */
    @NotNull(message = "不合格数量不能为空")
    private BigDecimal rejectedQuantity;

    /** 检验人 */
    private String inspector;

    /** 结论说明 */
    private String conclusion;

    /** 检验项明细 */
    private List<QcItemDTO> items;

    /**
     * 检验项 DTO
     */
    @Getter
    @Setter
    public static class QcItemDTO {

        /** 检验项名称 */
        private String name;

        /** 标准要求 */
        private String standard;

        /** 实际值 */
        private String actual;

        /** 检验结果：PASS/FAIL */
        private String result;
    }
}
