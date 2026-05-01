package com.jingwei.master.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物料分类树节点 VO
 * <p>
 * 用于返回分类树形结构，每个节点包含自身信息和子节点列表。
 * </p>
 *
 * @author JingWei
 */
@Data
public class CategoryTreeVO {

    /** 分类ID */
    private Long id;

    /** 父级分类ID */
    private Long parentId;

    /** 分类编码 */
    private String code;

    /** 分类名称 */
    private String name;

    /** 层级（1/2/3） */
    private Integer level;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态：ACTIVE/INACTIVE */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 子分类列表 */
    private List<CategoryTreeVO> children = new ArrayList<>();
}
