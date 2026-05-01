package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建库位请求 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateLocationDTO {

    /** 库区编码（如 A，必填） */
    @NotBlank(message = "库区编码不能为空")
    @Size(max = 16, message = "库区编码长度不能超过16个字符")
    private String zoneCode;

    /** 货架编码（如 01，必填） */
    @NotBlank(message = "货架编码不能为空")
    @Size(max = 16, message = "货架编码长度不能超过16个字符")
    private String rackCode;

    /** 层编码（如 02，必填） */
    @NotBlank(message = "层编码不能为空")
    @Size(max = 16, message = "层编码长度不能超过16个字符")
    private String rowCode;

    /** 位编码（如 03，必填） */
    @NotBlank(message = "位编码不能为空")
    @Size(max = 16, message = "位编码长度不能超过16个字符")
    private String binCode;

    /** 库位类型：STORAGE/PICKING/STAGING/QC（必填） */
    @NotBlank(message = "库位类型不能为空")
    private String locationType;

    /** 容量（可选） */
    private Integer capacity;

    /** 备注（可选） */
    private String remark;
}
