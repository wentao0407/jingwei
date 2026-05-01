package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改密码请求DTO
 * <p>
 * 用户修改自己的密码，需验证旧密码，新密码需满足密码策略。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class ChangePasswordDTO {

    /** 旧密码 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码（至少8位，必须含大写字母、小写字母和数字） */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度8-50个字符")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密码必须包含大写字母、小写字母和数字")
    private String newPassword;
}
