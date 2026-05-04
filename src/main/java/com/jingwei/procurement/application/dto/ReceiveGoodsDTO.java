package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收货确认 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ReceiveGoodsDTO {

    /** 到货通知单ID */
    @NotNull(message = "到货通知单ID不能为空")
    private Long asnId;

    /** 各行实收信息 */
    @NotEmpty(message = "收货行列表不能为空")
    private List<ReceiveLineDTO> lines;

    /**
     * 收货行 DTO
     */
    @Getter
    @Setter
    public static class ReceiveLineDTO {

        /** 到货行ID */
        @NotNull(message = "到货行ID不能为空")
        private Long lineId;

        /** 实收数量 */
        @NotNull(message = "实收数量不能为空")
        private BigDecimal receivedQuantity;
    }
}
