package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建物料分类请求 DTO
 * <p>
 * 入参使用 DTO 而非实体类，遵循开发规范。
 * level 不需要传入，由后端根据 parentId 自动计算。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateCategoryDTO {

    /** 父级分类ID（NULL 或不传表示顶级分类） */
    private Long parentId;

    /** 分类编码（同级唯一，必填） */
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 32, message = "分类编码长度不能超过32个字符")
    private String code;

    /** 分类名称（必填） */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    private String name;

    /** 排序号（可选，默认0） */
    private Integer sortOrder;
}
