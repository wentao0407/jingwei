package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import com.jingwei.common.domain.model.CommonStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 仓库实体
 * <p>
 * 对应数据库表 t_md_warehouse，管理仓库档案信息。
 * 仓库编码手动指定（如 WH01），不可重复。
 * 停用仓库后其下库位不可选用，但已有库存不受影响。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_warehouse")
public class Warehouse extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 仓库编码（手动指定，如 WH01，全局唯一） */
    private String code;

    /** 仓库名称 */
    private String name;

    /** 仓库类型：FINISHED_GOODS/RAW_MATERIAL/RETURN */
    private WarehouseType type;

    /** 地址 */
    private String address;

    /** 仓库管理员ID */
    private Long managerId;

    /** 状态：ACTIVE/INACTIVE */
    private CommonStatus status;

    /** 备注 */
    private String remark;

    /** 库位列表（详情查询时填充，非数据库字段） */
    @TableField(exist = false)
    private List<Location> locations;
}
