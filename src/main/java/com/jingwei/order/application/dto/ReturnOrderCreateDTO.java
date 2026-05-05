package com.jingwei.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建退货单 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ReturnOrderCreateDTO {

    /** 退货类型（CUSTOMER_REJECT/LOGISTICS_REJECT/DISTRIBUTOR_RETURN） */
    @NotBlank(message = "退货类型不能为空")
    private String returnType;

    /** 原销售订单ID */
    @NotNull(message = "原销售订单ID不能为空")
    private Long salesOrderId;

    /** 原销售订单编号 */
    @NotBlank(message = "原销售订单编号不能为空")
    private String salesOrderNo;

    /** 客户ID */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /** 退货原因 */
    private String reason;

    /** 备注 */
    private String remark;

    /** 退货行列表 */
    @NotEmpty(message = "退货行不能为空")
    @Valid
    private List<ReturnLineDTO> lines;

    /**
     * 退货行 DTO
     */
    @Getter
    @Setter
    public static class ReturnLineDTO {

        /** 原销售订单行ID */
        @NotNull(message = "原销售订单行ID不能为空")
        private Long salesOrderLineId;

        /** 款式ID */
        @NotNull(message = "款式ID不能为空")
        private Long spuId;

        /** 颜色款ID */
        @NotNull(message = "颜色款ID不能为空")
        private Long colorWayId;

        /** 退货尺码矩阵（JSON字符串，由前端构造） */
        @NotBlank(message = "尺码矩阵不能为空")
        private String sizeMatrixJson;

        /** 本行退货数量 */
        @NotNull(message = "退货数量不能为空")
        private Integer totalQuantity;

        /** 备注 */
        private String remark;
    }
}
