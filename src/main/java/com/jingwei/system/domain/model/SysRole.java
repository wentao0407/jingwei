package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色实体
 * <p>
 * 对应数据库表 t_sys_role，继承 BaseEntity 获得审计字段和乐观锁。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色编码 */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 角色描述 */
    private String description;

    /** 状态：ACTIVE/INACTIVE */
    private UserStatus status;
}
