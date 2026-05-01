package com.jingwei.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.*;
import com.jingwei.system.domain.model.SysMenu;
import com.jingwei.system.domain.model.SysRole;
import com.jingwei.system.domain.model.SysRoleMenu;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.SysUserRole;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.infrastructure.persistence.SysMenuMapper;
import com.jingwei.system.infrastructure.persistence.SysRoleMapper;
import com.jingwei.system.infrastructure.persistence.SysRoleMenuMapper;
import com.jingwei.system.infrastructure.persistence.SysUserMapper;
import com.jingwei.system.infrastructure.persistence.SysUserRoleMapper;
import com.jingwei.system.interfaces.vo.LoginVO;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-04 系统管理 — 用户与角色基础 集成测试
 * <p>
 * 注意：UserController 和 RoleController 的接口已添加 @RequirePermission 注解，
 * 测试用户需要拥有对应的菜单权限才能访问这些接口。
 * </p>
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

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    /** 认证 Token，所有请求需要携带 */
    private String authToken;

    @BeforeEach
    void setUp() {
        ensureTestUser();
    }

    @AfterEach
    void tearDown() {
        sysRoleMenuMapper.delete(null);
        sysUserRoleMapper.delete(null);
        sysMenuMapper.delete(null);
        sysUserMapper.delete(null);
        sysRoleMapper.delete(null);
    }

    @Test
    @DisplayName("创建用户时密码自动BCrypt加密，数据库中不存明文")
    void createUser_shouldEncryptPassword() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setUsername("testuser1");
        dto.setPassword("Plain123");
        dto.setRealName("测试用户");

        ResponseEntity<R<UserVO>> response = postWithAuth("/system/user/create", dto,
                new ParameterizedTypeReference<R<UserVO>>() {});

        assertTrue(response.getBody().isSuccess(), "创建用户应成功: " + response.getBody().getMessage());
        UserVO user = response.getBody().getData();
        assertNotNull(user.getId());
        assertEquals("testuser1", user.getUsername());

        // 验证数据库中密码不是明文
        SysUser dbUser = sysUserMapper.selectById(user.getId());
        assertNotEquals("Plain123", dbUser.getPassword(), "密码不应为明文");
        assertTrue(dbUser.getPassword().startsWith("$2a$"), "密码应为BCrypt格式");
    }

    @Test
    @DisplayName("同一用户名不可重复")
    void createUser_duplicateUsername_shouldFail() {
        CreateUserDTO dto1 = new CreateUserDTO();
        dto1.setUsername("duplicate");
        dto1.setPassword("Pass123123");
        postWithAuth("/system/user/create", dto1, new ParameterizedTypeReference<R<UserVO>>() {});

        CreateUserDTO dto2 = new CreateUserDTO();
        dto2.setUsername("duplicate");
        dto2.setPassword("Pass456456");

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
            dto.setPassword("Pass" + i + "12345");
            postWithAuth("/system/user/create", dto, new ParameterizedTypeReference<R<UserVO>>() {});
        }

        // 查询第1页，每页2条
        UserQueryDTO query = new UserQueryDTO();
        query.setCurrent(1);
        query.setSize(2);

        ResponseEntity<R<Page<UserVO>>> response = postWithAuth("/system/user/page", query,
                new ParameterizedTypeReference<R<Page<UserVO>>>() {});

        assertTrue(response.getBody().isSuccess(), "分页查询应成功: " + response.getBody().getMessage());
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
        createDto.setPassword("Pass123456");
        R<UserVO> created = postWithAuth("/system/user/create", createDto,
                new ParameterizedTypeReference<R<UserVO>>() {}).getBody();
        assertTrue(created.isSuccess(), "创建用户应成功: " + created.getMessage());
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
        userDto.setPassword("Pass123456");
        R<UserVO> createdUser = postWithAuth("/system/user/create", userDto,
                new ParameterizedTypeReference<R<UserVO>>() {}).getBody();
        assertTrue(createdUser.isSuccess(), "创建用户应成功: " + createdUser.getMessage());
        Long userId = createdUser.getData().getId();

        // 创建2个角色
        Long roleId1 = createRole("ADMIN_ROLE", "管理员");
        Long roleId2 = createRole("OPERATOR_ROLE", "操作员");

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
        assertTrue(createResp.getBody().isSuccess(), "创建角色应成功: " + createResp.getBody().getMessage());
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
     * 创建测试用户并分配全部菜单权限，确保能通过 @RequirePermission 校验
     * <p>
     * 流程：
     * 1. 通过 Mapper 创建测试用户（BCrypt 加密密码）
     * 2. 通过 Mapper 创建测试角色
     * 3. 为角色分配所有菜单权限
     * 4. 关联用户和角色
     * 5. 登录获取 Token
     * </p>
     */
    private void ensureTestUser() {
        // 1. 创建测试用户
        SysUser user = new SysUser();
        user.setUsername("authtestuser");
        user.setPassword(new BCryptPasswordEncoder().encode("Authpass123"));
        user.setRealName("认证测试用户");
        user.setStatus(UserStatus.ACTIVE);
        sysUserMapper.insert(user);

        // 2. 创建测试角色
        SysRole testRole = new SysRole();
        testRole.setRoleCode("TEST_FULL_PERM");
        testRole.setRoleName("测试全权限角色");
        testRole.setDescription("测试用，拥有全部菜单权限");
        testRole.setStatus(UserStatus.ACTIVE);
        sysRoleMapper.insert(testRole);

        // 3. 创建种子菜单（三级结构：目录→菜单→按钮）
        SysMenu seedDir = new SysMenu();
        seedDir.setParentId(0L);
        seedDir.setName("系统管理");
        seedDir.setType(com.jingwei.system.domain.model.MenuType.DIRECTORY);
        seedDir.setPath("/system");
        seedDir.setSortOrder(1);
        seedDir.setVisible(true);
        seedDir.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(seedDir);

        SysMenu seedMenu = new SysMenu();
        seedMenu.setParentId(seedDir.getId());
        seedMenu.setName("菜单管理");
        seedMenu.setType(com.jingwei.system.domain.model.MenuType.MENU);
        seedMenu.setPath("/system/menu");
        seedMenu.setComponent("system/MenuPage");
        seedMenu.setSortOrder(1);
        seedMenu.setVisible(true);
        seedMenu.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(seedMenu);

        insertButton(seedMenu.getId(), "创建菜单", "system:menu:create", 1);
        insertButton(seedMenu.getId(), "更新菜单", "system:menu:update", 2);
        insertButton(seedMenu.getId(), "删除菜单", "system:menu:delete", 3);
        insertButton(seedMenu.getId(), "分配权限", "system:role:assignPermission", 4);

        // 用户管理菜单 + 按钮
        SysMenu userMenu = new SysMenu();
        userMenu.setParentId(seedDir.getId());
        userMenu.setName("用户管理");
        userMenu.setType(com.jingwei.system.domain.model.MenuType.MENU);
        userMenu.setPath("/system/user");
        userMenu.setComponent("system/UserPage");
        userMenu.setSortOrder(2);
        userMenu.setVisible(true);
        userMenu.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(userMenu);

        insertButton(userMenu.getId(), "创建用户", "system:user:create", 1);
        insertButton(userMenu.getId(), "更新用户", "system:user:update", 2);
        insertButton(userMenu.getId(), "停用用户", "system:user:deactivate", 3);
        insertButton(userMenu.getId(), "分配角色", "system:user:assignRole", 4);

        // 角色管理菜单 + 按钮
        SysMenu roleMgmtMenu = new SysMenu();
        roleMgmtMenu.setParentId(seedDir.getId());
        roleMgmtMenu.setName("角色管理");
        roleMgmtMenu.setType(com.jingwei.system.domain.model.MenuType.MENU);
        roleMgmtMenu.setPath("/system/role");
        roleMgmtMenu.setComponent("system/RolePage");
        roleMgmtMenu.setSortOrder(3);
        roleMgmtMenu.setVisible(true);
        roleMgmtMenu.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(roleMgmtMenu);

        insertButton(roleMgmtMenu.getId(), "创建角色", "system:role:create", 1);
        insertButton(roleMgmtMenu.getId(), "更新角色", "system:role:update", 2);

        // 4. 为角色分配所有菜单权限
        List<SysMenu> allMenus = sysMenuMapper.selectList(null);
        for (SysMenu menu : allMenus) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(testRole.getId());
            roleMenu.setMenuId(menu.getId());
            sysRoleMenuMapper.insert(roleMenu);
        }

        // 5. 关联用户和角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(testRole.getId());
        sysUserRoleMapper.insert(userRole);

        // 6. 登录获取 Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("authtestuser");
        loginDTO.setPassword("Authpass123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<R<LoginVO>> response = restTemplate.exchange(
                "/auth/login", HttpMethod.POST, entity,
                new ParameterizedTypeReference<R<LoginVO>>() {});

        assertTrue(response.getBody().isSuccess(), "测试用户登录应成功: " + response.getBody().getMessage());
        authToken = response.getBody().getData().getToken();
    }

    private HttpHeaders authHeaders() {
        if (authToken == null) {
            ensureTestUser();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        return headers;
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
        assertTrue(result.isSuccess(), "创建角色应成功: " + result.getMessage());
        return result.getData().getId();
    }

    private void insertButton(Long parentId, String name, String permission, int sortOrder) {
        SysMenu button = new SysMenu();
        button.setParentId(parentId);
        button.setName(name);
        button.setType(com.jingwei.system.domain.model.MenuType.BUTTON);
        button.setPermission(permission);
        button.setSortOrder(sortOrder);
        button.setVisible(true);
        button.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(button);
    }
}
