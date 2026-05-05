package com.jingwei.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建出库单 DTO
 *
 * @author JingWei
 */
@Data
public class CreateOutboundDTO {

    /** 出库类型：SALES/MATERIAL/TRANSFER/RETURN_PURCHASE */
    @NotBlank(message = "出库类型不能为空")
    private String outboundType;

    /** 源仓库ID */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 来源单据类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据编号 */
    private String sourceNo;

    /** 物流公司 */
    private String carrier;

    /** 物流单号 */
    private String trackingNo;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    /** 出库单行列表 */
    @NotNull(message = "出库行不能为空")
    @Size(min = 1, message = "至少需要一行出库明细")
    private List<CreateOutboundLineDTO> lines;
}
