package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import com.jingwei.common.domain.model.CommonStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 物料分类实体
 * <p>
 * 对应数据库表 t_md_category，采用树形结构（parent_id）组织分类。
 * 最多支持 3 级分类，同级编码不可重复。
 * </p>
 * <p>
 * 典型分类结构（服装行业）：
 * <pre>
 * ├── 成品
 * │   ├── 女装
 * │   │   ├── 外套
 * │   │   ├── 连衣裙
 * │   │   └── 裤装
 * │   ├── 男装
 * │   └── 童装
 * ├── 面料
 * │   ├── 梭织面料
 * │   └── 针织面料
 * └── 辅料
 *     ├── 纽扣
 *     └── 拉链
 * </pre>
 * </p>
 * <p>
 * children 字段不持久化到数据库（@TableField(exist = false)），
 * 仅在查询树形结构时由 CategoryDomainService.buildTree() 组装填充。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_category")
public class Category extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父级分类ID（NULL 表示顶级分类） */
    private Long parentId;

    /** 分类编码（同级唯一） */
    private String code;

    /** 分类名称 */
    private String name;

    /** 层级（1/2/3，自动计算） */
    private Integer level;

    /** 排序号（同级内的显示顺序） */
    private Integer sortOrder;

    /** 状态：ACTIVE/INACTIVE */
    private CommonStatus status;

    /** 子分类列表（非数据库字段，组装树时填充） */
    @TableField(exist = false)
    private List<Category> children = new ArrayList<>();

    /**
     * 添加子分类节点
     *
     * @param child 子分类
     */
    public void addChild(Category child) {
        this.children.add(child);
    }
}
