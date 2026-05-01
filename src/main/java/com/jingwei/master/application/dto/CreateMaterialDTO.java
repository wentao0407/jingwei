package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 创建物料请求 DTO
 * <p>
 * 物料编码（code）不需要前端传入，由后端调用编码规则引擎自动生成。
 * </p>
 *
 * @author JingWei
 */
@Data
public class CreateMaterialDTO {

    /** 物料名称（必填） */
    @NotBlank(message = "物料名称不能为空")
    @Size(max = 128, message = "物料名称长度不能超过128个字符")
    private String name;

    /** 物料类型：FABRIC/TRIM/PACKAGING（必填，不含 PRODUCT） */
    @NotBlank(message = "物料类型不能为空")
    private String type;

    /** 物料分类ID（可选） */
    private Long categoryId;

    /** 基本单位（必填，如 米/个/套/件） */
    @NotBlank(message = "基本单位不能为空")
    private String unit;

    /** 扩展属性（按 type 不同存不同结构） */
    private Map<String, Object> extAttrs;

    /** 备注 */
    private String remark;
}
