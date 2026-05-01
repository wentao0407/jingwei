package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建用户请求DTO
 * <p>
 * 密码策略：至少8位，必须包含大写字母、小写字母和数字。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateUserDTO {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名最长50个字符")
    private String username;

    /** 密码（至少8位，必须含大写字母、小写字母和数字） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度8-50个字符")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密码必须包含大写字母、小写字母和数字")
    private String password;

    /** 真实姓名 */
    @Size(max = 50, message = "姓名最长50个字符")
    private String realName;

    /** 手机号 */
    @Size(max = 20, message = "手机号最长20个字符")
    private String phone;

    /** 邮箱 */
    @Size(max = 100, message = "邮箱最长100个字符")
    private String email;
}
