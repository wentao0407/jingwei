package com.jingwei.system.domain.model;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author JingWei
 */
@Getter
public enum UserStatus {

    /** 正常 */
    ACTIVE("正常"),
    /** 停用 */
    INACTIVE("停用");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
