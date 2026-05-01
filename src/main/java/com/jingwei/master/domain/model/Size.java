package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 尺码实体
 * <p>
 * 对应数据库表 t_md_size，属于某个尺码组内的一个尺码项。
 * sort_order 决定尺码在矩阵视图中的列顺序。
 * </p>
 * <p>
 * 注意：Size 不继承 BaseEntity 的审计字段用于业务追踪，
 * 但不设独立的 status 字段——尺码的生命周期由所属尺码组管理。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>同一尺码组内尺码编码不可重复</li>
 *   <li>已被 SPU 引用的尺码组中，不可删除或修改已有尺码的编码</li>
 *   <li>已被 SPU 引用的尺码组中，可以新增尺码（追加到末尾）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_size")
public class Size extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属尺码组ID */
    private Long sizeGroupId;

    /** 尺码编码（如 S/M/L/XL/XXL 或 160/165/170），组内唯一 */
    private String code;

    /** 尺码名称（如 S、M、L），用于前端展示 */
    private String name;

    /** 排序号（决定矩阵中的列顺序，值越小越靠前） */
    private Integer sortOrder;
}
