package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 登录响应VO
 * <p>
 * 登录成功后返回用户信息和 Token。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
