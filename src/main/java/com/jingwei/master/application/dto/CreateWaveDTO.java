package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建波段请求 DTO
 * <p>
 * 在指定季节下新增一个波段。
 * sortOrder 不传时自动追加到末尾。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateWaveDTO {

    /** 波段编码（组内唯一，必填，如 2026SS-W1） */
    @NotBlank(message = "波段编码不能为空")
    @Size(max = 16, message = "波段编码长度不能超过16个字符")
    private String code;

    /** 波段名称（必填，如 春一） */
    @NotBlank(message = "波段名称不能为空")
    @Size(max = 64, message = "波段名称长度不能超过64个字符")
    private String name;

    /** 交货日期（可选） */
    private java.time.LocalDate deliveryDate;

    /** 排序号（可选，不传则追加到末尾） */
    private Integer sortOrder;
}
