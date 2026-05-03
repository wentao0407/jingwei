package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 尺码条目 DTO
 * <p>
 * 表示尺码矩阵中一个尺码及其数量。
 * </p>
 *
 * @author JingWei
 */
@Data
public class SizeEntryDTO {

    /** 尺码ID */
    @NotNull(message = "尺码ID不能为空")
    private Long sizeId;

    /** 尺码编码（如 S/M/L） */
    @NotNull(message = "尺码编码不能为空")
    private String code;

    /** 数量 */
    private int quantity;
}
