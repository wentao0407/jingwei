package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建收货单 DTO
 *
 * @author JingWei
 */
@Data
public class CreateReceivingDTO {

    /** 到货通知单ID */
    @NotNull(message = "ASN ID不能为空")
    private Long asnId;

    /** 收货仓库ID */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 收货月台号 */
    private String dockNo;
}
