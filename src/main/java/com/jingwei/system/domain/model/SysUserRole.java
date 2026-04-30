package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户角色关联实体
 * <p>
 * 对应数据库表 t_sys_user_role，用于用户和角色的多对多关联。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_user_role")
public class SysUserRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;
}
