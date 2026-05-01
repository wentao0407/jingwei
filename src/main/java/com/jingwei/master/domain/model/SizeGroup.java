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
 * 尺码组实体
 * <p>
 * 对应数据库表 t_md_size_group，管理不同品类的尺码体系。
 * 每个尺码组包含一组有序的尺码，创建款式（SPU）时选择对应尺码组。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>尺码组编码全局唯一</li>
 *   <li>尺码组被 SPU 引用后不可删除，但可停用</li>
 *   <li>已被引用的尺码组可以新增尺码（追加到末尾）</li>
 *   <li>已被引用的尺码组不可删除或修改已有尺码的编码</li>
 * </ul>
 * </p>
 * <p>
 * sizes 字段不持久化到数据库（@TableField(exist = false），
 * 仅在查询时由 SizeGroupDomainService 组装填充。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_size_group")
public class SizeGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 尺码组编码（如 WOMEN_STD），全局唯一 */
    private String code;

    /** 尺码组名称（如 女装标准码） */
    private String name;

    /** 适用品类：WOMEN/MEN/CHILDREN */
    private SizeCategory category;

    /** 状态：ACTIVE/INACTIVE */
    private CommonStatus status;

    /** 尺码列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<Size> sizes = new ArrayList<>();

    /**
     * 添加尺码到列表
     *
     * @param size 尺码实体
     */
    public void addSize(Size size) {
        this.sizes.add(size);
    }
}
