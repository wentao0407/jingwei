package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新物料分类请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 不允许变更 parentId 和 level（如需移动分类，应删除后重建）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateCategoryDTO {

    /** 分类编码（可选，修改时校验同级唯一性） */
    @Size(max = 32, message = "分类编码长度不能超过32个字符")
    private String code;

    /** 分类名称（可选） */
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    private String name;

    /** 排序号（可选） */
    private Integer sortOrder;

    /** 状态：ACTIVE/INACTIVE（可选） */
    private String status;
}
