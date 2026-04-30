package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String status;

    private List<Long> roleIds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
