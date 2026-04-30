package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体
 * <p>
 * 对应数据库表 t_sys_user，继承 BaseEntity 获得审计字段和乐观锁。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：ACTIVE/INACTIVE */
    private UserStatus status;
}
