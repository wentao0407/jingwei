package com.jingwei.master.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 更新物料请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 物料编码和类型不可修改。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateMaterialDTO {

    /** 物料名称（可选） */
    private String name;

    /** 物料分类ID（可选） */
    private Long categoryId;

    /** 基本单位（可选） */
    private String unit;

    /** 扩展属性（可选，传入则整体替换） */
    private Map<String, Object> extAttrs;

    /** 备注（可选） */
    private String remark;

    /** 状态：ACTIVE/INACTIVE（可选） */
    private String status;
}
