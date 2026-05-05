package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户仓库权限实体
 * <p>
 * 对应数据库表 t_sys_user_warehouse，控制用户可访问的仓库范围。
 * 当角色 data_scope = WAREHOUSE 时，通过此表过滤数据。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_user_warehouse")
public class SysUserWarehouse extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 仓库ID */
    private Long warehouseId;
}
