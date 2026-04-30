package com.jingwei.system.interfaces.controller;

import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.LoginDTO;
import com.jingwei.system.application.service.AuthApplicationService;
import com.jingwei.system.interfaces.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller
 * <p>
 * 提供登录接口，登录成功返回 JWT Token。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    /**
     * 登录接口
     *
     * @param dto 登录请求（用户名+密码）
     * @return 登录响应（Token + 用户信息）
     */
    @PostMapping("/auth/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authApplicationService.login(dto));
    }
}
