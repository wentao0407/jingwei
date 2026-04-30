package com.jingwei.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.*;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.infrastructure.persistence.SysRoleMapper;
import com.jingwei.system.infrastructure.persistence.SysUserMapper;
import com.jingwei.system.infrastructure.persistence.SysUserRoleMapper;
import com.jingwei.system.interfaces.vo.RoleVO;
import com.jingwei.system.interfaces.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-04 系统管理 — 用户与角色基础 集成测试
 *
 * @author JingWei
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemUserAndRoleTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /** 认证 Token，所有请求需要携带 */
    private String authToken;

    @BeforeEach
    void setUp() {
        // 先通过 Mapper 直接创建测试用户（不经过 HTTP，避免认证问题）
        SysUser admin = new SysUser();
        admin.setUsername("testadmin");
        admin.setPassword("$2a$10$dummybcryptpasswordhashfortest");
        admin.setRealName("测试管理员");
        admin.setStatus(UserStatus.ACTIVE);
        sysUserMapper.insert(admin);

        // 通过登录接口获取 Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testadmin");
        loginDTO.setPassword("dummybcryptpasswordhashfortest"); // 这不会匹配，需要用真实密码

        // 直接用 Service 层创建一个可登录的用户更好，这里用简单方案：
        // 在测试中先用 permitAll 的方式（通过 withBasicAuth）
    }

    @AfterEach
    void tearDown() {
        sysUserRoleMapper.delete(null);
        sysUserMapper.delete(null);
        sysRoleMapper.delete(null);
    }

    @Test
    @DisplayName("创建用户时密码自动BCrypt加密，数据库中不存明文")
    void createUser_shouldEncryptPassword() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setUsername("testuser1");
        dto.setPassword("plain123");
        dto.setRealName("测试用户");

        ResponseEntity<R<UserVO>> response = postWithAuth("/system/user/create", dto,
                new ParameterizedTypeReference<R<UserVO>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserVO user = response.getBody().getData();
        assertNotNull(user.getId());
        assertEquals("testuser1", user.getUsername());

        // 验证数据库中密码不是明文
        SysUser dbUser = sysUserMapper.selectById(user.getId());
        assertNotEquals("plain123", dbUser.getPassword(), "密码不应为明文");
        assertTrue(dbUser.getPassword().startsWith("$2a$"), "密码应为BCrypt格式");
    }

    @Test
    @DisplayName("同一用户名不可重复")
    void createUser_duplicateUsername_shouldFail() {
        CreateUserDTO dto1 = new CreateUserDTO();
        dto1.setUsername("duplicate");
        dto1.setPassword("pass123123");
        postWithAuth("/system/user/create", dto1, new ParameterizedTypeReference<R<UserVO>>() {});

        CreateUserDTO dto2 = new CreateUserDTO();
        dto2.setUsername("duplicate");
        dto2.setPassword("pass456456");

        ResponseEntity<R<UserVO>> response = postWithAuth("/system/user/create", dto2,
                new ParameterizedTypeReference<R<UserVO>>() {});

        // 应该返回业务错误
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("用户列表支持分页查询")
    void pageUser_shouldReturnPagedResult() {
        // 创建3个用户
        for (int i = 1; i <= 3; i++) {
            CreateUserDTO dto = new CreateUserDTO();
            dto.setUsername("pageuser" + i);
            dto.setPassword("pass" + i + "12345");
            postWithAuth("/system/user/create", dto, new ParameterizedTypeReference<R<UserVO>>() {});
        }

        // 查询第1页，每页2条
        UserQueryDTO query = new UserQueryDTO();
        query.setCurrent(1);
        query.setSize(2);

        ResponseEntity<R<Page<UserVO>>> response = postWithAuth("/system/user/page", query,
                new ParameterizedTypeReference<R<Page<UserVO>>>() {});

        assertTrue(response.getBody().isSuccess());
        Page<UserVO> page = response.getBody().getData();
        assertEquals(2, page.getRecords().size());
        assertTrue(page.getTotal() >= 3);
    }

    @Test
    @DisplayName("用户可停用，停用后状态为INACTIVE")
    void deactivateUser_shouldSetInactive() {
        // 先创建用户
        CreateUserDTO createDto = new CreateUserDTO();
        createDto.setUsername("deactivate");
        createDto.setPassword("pass123456");
        R<UserVO> created = postWithAuth("/system/user/create", createDto,
                new ParameterizedTypeReference<R<UserVO>>() {}).getBody();
        Long userId = created.getData().getId();

        // 停用
        ResponseEntity<R<Void>> response = postWithAuthNoBody("/system/user/deactivate?userId=" + userId,
                new ParameterizedTypeReference<R<Void>>() {});

        assertTrue(response.getBody().isSuccess());

        // 验证状态
        SysUser dbUser = sysUserMapper.selectById(userId);
        assertEquals(UserStatus.INACTIVE, dbUser.getStatus());
    }

    @Test
    @DisplayName("可为用户分配多个角色")
    void assignRoles_shouldWork() {
        // 创建用户
        CreateUserDTO userDto = new CreateUserDTO();
        userDto.setUsername("roletest");
        userDto.setPassword("pass123456");
        Long userId = postWithAuth("/system/user/create", userDto,
                new ParameterizedTypeReference<R<UserVO>>() {}).getBody().getData().getId();

        // 创建2个角色
        Long roleId1 = createRole("ADMIN", "管理员");
        Long roleId2 = createRole("OPERATOR", "操作员");

        // 分配角色
        AssignRoleDTO assignDto = new AssignRoleDTO();
        assignDto.setRoleIds(List.of(roleId1, roleId2));

        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AssignRoleDTO> entity = new HttpEntity<>(assignDto, headers);

        ResponseEntity<R<Void>> response = restTemplate.exchange(
                "/system/user/assignRoles?userId=" + userId,
                HttpMethod.POST, entity, new ParameterizedTypeReference<R<Void>>() {});

        assertTrue(response.getBody().isSuccess());

        // 验证用户详情中包含角色ID
        ResponseEntity<R<UserVO>> detailResp = postWithAuthNoBody("/system/user/detail?userId=" + userId,
                new ParameterizedTypeReference<R<UserVO>>() {});
        UserVO userDetail = detailResp.getBody().getData();
        assertEquals(2, userDetail.getRoleIds().size());
        assertTrue(userDetail.getRoleIds().contains(roleId1));
        assertTrue(userDetail.getRoleIds().contains(roleId2));
    }

    @Test
    @DisplayName("角色CRUD — 创建、查询、更新")
    void roleCrud_shouldWork() {
        // 创建角色
        CreateRoleDTO createDto = new CreateRoleDTO();
        createDto.setRoleCode("TEST_ROLE");
        createDto.setRoleName("测试角色");
        createDto.setDescription("测试用");

        ResponseEntity<R<RoleVO>> createResp = postWithAuth("/system/role/create", createDto,
                new ParameterizedTypeReference<R<RoleVO>>() {});
        assertTrue(createResp.getBody().isSuccess());
        RoleVO created = createResp.getBody().getData();
        assertEquals("TEST_ROLE", created.getRoleCode());
        assertEquals("测试角色", created.getRoleName());

        // 更新角色
        UpdateRoleDTO updateDto = new UpdateRoleDTO();
        updateDto.setRoleName("更新后角色名");
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateRoleDTO> entity = new HttpEntity<>(updateDto, headers);

        ResponseEntity<R<RoleVO>> updateResp = restTemplate.exchange(
                "/system/role/update?roleId=" + created.getId(),
                HttpMethod.POST, entity, new ParameterizedTypeReference<R<RoleVO>>() {});
        assertTrue(updateResp.getBody().isSuccess());
        assertEquals("更新后角色名", updateResp.getBody().getData().getRoleName());
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取带认证头的 HttpHeaders
     * <p>
     * 使用 TestRestTemplate 的 withBasicAuth 方法进行认证。
     * 但由于我们使用 JWT，这里用更简单的方式：先登录获取 Token。
     * 为了测试简便，直接通过 Mapper 创建用户再用密码登录。
     * </p>
     */
    private HttpHeaders authHeaders() {
        // 每次获取 Token 前先确保有一个可登录的用户
        if (authToken == null) {
            ensureTestUser();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        return headers;
    }

    private void ensureTestUser() {
        // 检查是否已存在测试用户
        SysUser existing = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, "authtestuser"));
        if (existing == null) {
            // 通过 create 接口创建（此时还没 Token，用 withBasicAuth 不行）
            // 改为直接通过 Mapper 创建，密码用 BCrypt 加密
            SysUser user = new SysUser();
            user.setUsername("authtestuser");
            user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("authpass123"));
            user.setRealName("认证测试用户");
            user.setStatus(UserStatus.ACTIVE);
            sysUserMapper.insert(user);
        }

        // 登录获取 Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("authtestuser");
        loginDTO.setPassword("authpass123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<R<com.jingwei.system.interfaces.vo.LoginVO>> response = restTemplate.exchange(
                "/auth/login", HttpMethod.POST, entity,
                new ParameterizedTypeReference<R<com.jingwei.system.interfaces.vo.LoginVO>>() {});

        authToken = response.getBody().getData().getToken();
    }

    private <T> ResponseEntity<R<T>> postWithAuth(String url, Object body, ParameterizedTypeReference<R<T>> typeRef) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
    }

    private <T> ResponseEntity<R<T>> postWithAuthNoBody(String url, ParameterizedTypeReference<R<T>> typeRef) {
        HttpHeaders headers = authHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
    }

    private Long createRole(String code, String name) {
        CreateRoleDTO dto = new CreateRoleDTO();
        dto.setRoleCode(code);
        dto.setRoleName(name);
        R<RoleVO> result = postWithAuth("/system/role/create", dto,
                new ParameterizedTypeReference<R<RoleVO>>() {}).getBody();
        return result.getData().getId();
    }
}
