package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建数量变更单 DTO
 * <p>
 * 已确认的销售订单如需修改行数量，通过此 DTO 发起变更单。
 * </p>
 *
 * @author JingWei
 */
@Data
public class QuantityChangeCreateDTO {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 订单行ID */
    @NotNull(message = "订单行ID不能为空")
    private Long orderLineId;

    /** 变更后的尺码组ID */
    @NotNull(message = "尺码组ID不能为空")
    private Long sizeGroupId;

    /** 变更后的尺码数量列表 */
    @NotNull(message = "尺码矩阵不能为空")
    private List<SizeEntryDTO> sizes;

    /** 变更原因 */
    @NotBlank(message = "变更原因不能为空")
    @Size(max = 500, message = "变更原因不能超过500字")
    private String reason;
}
