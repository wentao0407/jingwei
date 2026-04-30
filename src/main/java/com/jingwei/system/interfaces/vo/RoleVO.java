package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 角色响应VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
