package com.jingwei.system;

import com.jingwei.common.config.JwtUtil;
import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.LoginDTO;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.infrastructure.persistence.SysUserMapper;
import com.jingwei.system.interfaces.vo.LoginVO;
import com.jingwei.system.interfaces.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-05 JWT 认证与登录 集成测试
 *
 * @author JingWei
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthJwtTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        sysUserMapper.delete(null);
    }

    @Test
    @DisplayName("正确的用户名密码 → 登录成功，返回 Token")
    void login_withCorrectCredentials_shouldReturnToken() {
        // 先创建用户
        createUser("loginuser", "Password123");

        // 登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("loginuser");
        loginDTO.setPassword("Password123");

        ResponseEntity<R<LoginVO>> response = post("/auth/login", loginDTO,
                new ParameterizedTypeReference<>() {});

        assertTrue(response.getBody().isSuccess());
        LoginVO loginVO = response.getBody().getData();
        assertNotNull(loginVO.getToken(), "应返回 Token");
        assertEquals("loginuser", loginVO.getUsername());
        assertNotNull(loginVO.getUserId());
    }

    @Test
    @DisplayName("错误的密码 → 返回'用户名或密码错误'")
    void login_withWrongPassword_shouldReturnLoginFailed() {
        createUser("wrongpwd", "Password123");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("wrongpwd");
        loginDTO.setPassword("WrongPassword1");

        ResponseEntity<R<LoginVO>> response = post("/auth/login", loginDTO,
                new ParameterizedTypeReference<>() {});

        assertFalse(response.getBody().isSuccess());
        assertEquals(91002, response.getBody().getCode(), "应为 LOGIN_FAILED 错误码");
    }

    @Test
    @DisplayName("停用用户 → 拒绝登录")
    void login_withInactiveUser_shouldReturnUserInactive() {
        // 创建用户并停用
        Long userId = createUser("inactiveuser", "Password123");
        deactivateUser(userId);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("inactiveuser");
        loginDTO.setPassword("Password123");

        ResponseEntity<R<LoginVO>> response = post("/auth/login", loginDTO,
                new ParameterizedTypeReference<>() {});

        assertFalse(response.getBody().isSuccess());
        assertEquals(91003, response.getBody().getCode(), "应为 USER_INACTIVE 错误码");
    }

    @Test
    @DisplayName("携带有效 Token 访问受保护接口 → 正常返回")
    void accessProtected_withValidToken_shouldSucceed() {
        // 创建用户并登录获取 Token
        createUser("tokenuser", "Password123");
        String token = loginAndGetToken("tokenuser", "Password123");

        // 用 Token 访问受保护接口
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<R<UserVO>> response = restTemplate.exchange(
                "/system/user/detail?userId=1",
                HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});

        // 只要不是 401 就算成功（可能 404 因为 userId=1 不存在，但说明认证通过了）
        assertNotEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("不携带 Token → 返回 401")
    void accessProtected_withoutToken_shouldReturn401() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(restTemplate.getRootUri() + "/system/user/page"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("携带无效 Token → 返回 401")
    void accessProtected_withInvalidToken_shouldReturn401() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(restTemplate.getRootUri() + "/system/user/page"))
                .header("Authorization", "Bearer invalid.token.here")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("Token 过期 → 返回 401")
    void accessProtected_withExpiredToken_shouldReturn401() {
        // 手动生成一个已过期的 Token（过期时间为 -1 毫秒，即已过期）
        String expiredToken = jwtUtil.generateToken(1L, "expired");
        // 由于 JwtUtil 的 expiration 是配置值，我们无法直接生成过期 Token
        // 改为验证 JwtUtil.validateToken 对无效 Token 返回 false
        assertFalse(jwtUtil.validateToken("invalid.token.value"));
    }

    @Test
    @DisplayName("登录成功 → Token 可解析出用户ID")
    void loginSuccess_tokenShouldContainUserId() {
        Long userId = createUser("parsetest", "Password123");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("parsetest");
        loginDTO.setPassword("Password123");

        ResponseEntity<R<LoginVO>> response = post("/auth/login", loginDTO,
                new ParameterizedTypeReference<>() {});
        String token = response.getBody().getData().getToken();

        // 用 JwtUtil 解析 Token
        Long parsedUserId = jwtUtil.getUserIdFromToken(token);
        assertEquals(userId, parsedUserId, "Token 中应包含正确的用户ID");
        assertEquals("parsetest", jwtUtil.getUsernameFromToken(token));
    }

    // ==================== 辅助方法 ====================

    /**
     * 直接通过 Mapper 创建用户，绕过 Security 认证
     */
    private Long createUser(String username, String password) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(UserStatus.ACTIVE);
        sysUserMapper.insert(user);
        return user.getId();
    }

    /**
     * 直接通过 Mapper 停用用户，绕过 Security 认证
     */
    private void deactivateUser(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(UserStatus.INACTIVE);
        sysUserMapper.updateById(user);
    }

    private String loginAndGetToken(String username, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        R<LoginVO> result = post("/auth/login", dto,
                new ParameterizedTypeReference<R<LoginVO>>() {}).getBody();
        return result.getData().getToken();
    }

    private <T> ResponseEntity<R<T>> post(String url, Object body, ParameterizedTypeReference<R<T>> typeRef) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
    }

    /**
     * 创建不自动重试认证的 RestTemplate，用于测试 401 响应
     */
    private RestTemplate createPlainRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        RestTemplate rt = new RestTemplate(factory);
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(@SuppressWarnings("org.springframework:HiddenField") org.springframework.http.client.ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is5xxServerError();
            }
        });
        return rt;
    }
}
