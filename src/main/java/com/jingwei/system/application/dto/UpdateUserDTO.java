package com.jingwei.system.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新用户请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateUserDTO {

    /** 真实姓名 */
    @Size(max = 50, message = "姓名最长50个字符")
    private String realName;

    /** 手机号 */
    @Size(max = 20, message = "手机号最长20个字符")
    private String phone;

    /** 邮箱 */
    @Size(max = 100, message = "邮箱最长100个字符")
    private String email;

    /** 状态：ACTIVE/INACTIVE */
    private String status;
}
