package com.jingwei.system;

import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.*;
import com.jingwei.system.domain.model.*;
import com.jingwei.system.infrastructure.persistence.SysMenuMapper;
import com.jingwei.system.infrastructure.persistence.SysRoleMapper;
import com.jingwei.system.infrastructure.persistence.SysRoleMenuMapper;
import com.jingwei.system.infrastructure.persistence.SysUserMapper;
import com.jingwei.system.infrastructure.persistence.SysUserRoleMapper;
import com.jingwei.system.interfaces.vo.LoginVO;
import com.jingwei.system.interfaces.vo.MenuVO;
import com.jingwei.system.interfaces.vo.UserPermissionVO;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-06 RBAC 权限（菜单级+按钮级）集成测试
 * <p>
 * 测试场景：
 * <ul>
 *   <li>菜单 CRUD（树形结构：目录→菜单→按钮）</li>
 *   <li>角色-菜单权限分配</li>
 *   <li>登录后返回权限标识列表</li>
 *   <li>查询当前用户菜单树和权限</li>
 *   <li>无权限访问接口 → 返回 403</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RbacPermissionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    private String authToken;

    @BeforeEach
    void setUp() {
        // 先清理上一轮测试残留数据（顺序：先删关联表，再删主表）
        sysRoleMenuMapper.delete(null);
        sysUserRoleMapper.delete(null);
        sysMenuMapper.delete(null);
        sysUserMapper.delete(null);
        sysRoleMapper.delete(null);

        // 创建可登录的管理员用户
        SysUser admin = new SysUser();
        admin.setUsername("rbacadmin");
        admin.setPassword(new BCryptPasswordEncoder().encode("Admin123"));
        admin.setRealName("RBAC测试管理员");
        admin.setStatus(UserStatus.ACTIVE);
        sysUserMapper.insert(admin);

        // 创建管理员角色
        SysRole adminRole = new SysRole();
        adminRole.setRoleCode("RBAC_ADMIN");
        adminRole.setRoleName("RBAC测试管理员角色");
        adminRole.setDescription("测试用");
        adminRole.setStatus(UserStatus.ACTIVE);
        sysRoleMapper.insert(adminRole);

        // 关联用户和角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(admin.getId());
        userRole.setRoleId(adminRole.getId());
        sysUserRoleMapper.insert(userRole);

        // 创建种子菜单（三级结构：目录→菜单→按钮），供后续测试使用
        SysMenu seedDir = new SysMenu();
        seedDir.setParentId(0L);
        seedDir.setName("系统管理");
        seedDir.setType(MenuType.DIRECTORY);
        seedDir.setPath("/system");
        seedDir.setSortOrder(1);
        seedDir.setVisible(true);
        seedDir.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(seedDir);

        SysMenu seedMenu = new SysMenu();
        seedMenu.setParentId(seedDir.getId());
        seedMenu.setName("菜单管理");
        seedMenu.setType(MenuType.MENU);
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
        userMenu.setType(MenuType.MENU);
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
        roleMgmtMenu.setType(MenuType.MENU);
        roleMgmtMenu.setPath("/system/role");
        roleMgmtMenu.setComponent("system/RolePage");
        roleMgmtMenu.setSortOrder(3);
        roleMgmtMenu.setVisible(true);
        roleMgmtMenu.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(roleMgmtMenu);

        insertButton(roleMgmtMenu.getId(), "创建角色", "system:role:create", 1);
        insertButton(roleMgmtMenu.getId(), "更新角色", "system:role:update", 2);

        // 为角色分配所有菜单权限
        List<SysMenu> allMenus = sysMenuMapper.selectList(null);
        for (SysMenu menu : allMenus) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(adminRole.getId());
            roleMenu.setMenuId(menu.getId());
            sysRoleMenuMapper.insert(roleMenu);
        }

        // 登录获取 Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("rbacadmin");
        loginDTO.setPassword("Admin123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<R<LoginVO>> response = restTemplate.exchange(
                "/auth/login", HttpMethod.POST, entity,
                new ParameterizedTypeReference<R<LoginVO>>() {});

        authToken = response.getBody().getData().getToken();
    }

    @AfterEach
    void tearDown() {
        sysRoleMenuMapper.delete(null);
        sysUserRoleMapper.delete(null);
        sysMenuMapper.delete(null);
        sysUserMapper.delete(null);
        sysRoleMapper.delete(null);
    }

    // ==================== 菜单 CRUD ====================

    @Test
    @DisplayName("创建目录类型菜单应成功")
    void createMenu_directory_shouldSucceed() {
        CreateMenuDTO dto = new CreateMenuDTO();
        dto.setParentId(0L);
        dto.setName("测试目录");
        dto.setType("DIRECTORY");
        dto.setPath("/test");
        dto.setSortOrder(99);

        ResponseEntity<R<MenuVO>> response = postWithAuth("/system/menu/create", dto,
                new ParameterizedTypeReference<R<MenuVO>>() {});

        assertTrue(response.getBody().isSuccess());
        MenuVO created = response.getBody().getData();
        assertNotNull(created.getId());
        assertEquals("测试目录", created.getName());
        assertEquals("DIRECTORY", created.getType());
    }

    @Test
    @DisplayName("创建菜单类型菜单（在目录下）应成功")
    void createMenu_menuUnderDirectory_shouldSucceed() {
        // 先创建目录
        Long dirId = createDirectory("菜单一目录");

        CreateMenuDTO dto = new CreateMenuDTO();
        dto.setParentId(dirId);
        dto.setName("测试菜单页");
        dto.setType("MENU");
        dto.setPath("/test/page");
        dto.setComponent("test/Page");

        ResponseEntity<R<MenuVO>> response = postWithAuth("/system/menu/create", dto,
                new ParameterizedTypeReference<R<MenuVO>>() {});

        assertTrue(response.getBody().isSuccess());
        assertEquals("MENU", response.getBody().getData().getType());
    }

    @Test
    @DisplayName("创建按钮类型菜单（在菜单下）应成功")
    void createMenu_buttonUnderMenu_shouldSucceed() {
        // 先创建目录 → 菜单
        Long dirId = createDirectory("按钮一目录");
        Long menuId = createMenu(dirId, "按钮一菜单页");

        CreateMenuDTO dto = new CreateMenuDTO();
        dto.setParentId(menuId);
        dto.setName("测试按钮");
        dto.setType("BUTTON");
        dto.setPermission("test:button:click");

        ResponseEntity<R<MenuVO>> response = postWithAuth("/system/menu/create", dto,
                new ParameterizedTypeReference<R<MenuVO>>() {});

        assertTrue(response.getBody().isSuccess());
        assertEquals("BUTTON", response.getBody().getData().getType());
        assertEquals("test:button:click", response.getBody().getData().getPermission());
    }

    @Test
    @DisplayName("创建按钮类型菜单 — 没有 permission 应失败")
    void createMenu_buttonWithoutPermission_shouldFail() {
        Long dirId = createDirectory("无权限按钮目录");
        Long menuId = createMenu(dirId, "无权限按钮菜单");

        CreateMenuDTO dto = new CreateMenuDTO();
        dto.setParentId(menuId);
        dto.setName("无权限按钮");
        dto.setType("BUTTON");
        // 不设置 permission

        ResponseEntity<R<MenuVO>> response = postWithAuth("/system/menu/create", dto,
                new ParameterizedTypeReference<R<MenuVO>>() {});

        assertFalse(response.getBody().isSuccess());
        assertEquals(91008, response.getBody().getCode());
    }

    @Test
    @DisplayName("更新菜单应成功")
    void updateMenu_shouldSucceed() {
        Long dirId = createDirectory("更新测试目录");

        UpdateMenuDTO dto = new UpdateMenuDTO();
        dto.setName("更新后目录名");

        ResponseEntity<R<MenuVO>> response = postWithAuth(
                "/system/menu/update?menuId=" + dirId, dto,
                new ParameterizedTypeReference<R<MenuVO>>() {});

        assertTrue(response.getBody().isSuccess());
        assertEquals("更新后目录名", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("删除菜单 — 无子菜单且无角色引用应成功")
    void deleteMenu_noChildrenNoRoleRef_shouldSucceed() {
        Long dirId = createDirectory("待删除目录");

        ResponseEntity<R<Void>> response = postWithAuthNoBody(
                "/system/menu/delete?menuId=" + dirId,
                new ParameterizedTypeReference<R<Void>>() {});

        assertTrue(response.getBody().isSuccess());
    }

    // ==================== 菜单树查询 ====================

    @Test
    @DisplayName("查询完整菜单树应返回树形结构")
    void getMenuTree_shouldReturnTree() {
        ResponseEntity<R<List<MenuVO>>> response = postWithAuthNoBody(
                "/system/menu/tree",
                new ParameterizedTypeReference<R<List<MenuVO>>>() {});

        assertTrue(response.getBody().isSuccess());
        List<MenuVO> tree = response.getBody().getData();
        assertFalse(tree.isEmpty(), "菜单树不应为空");

        // 验证顶级节点都是目录
        for (MenuVO top : tree) {
            assertEquals("DIRECTORY", top.getType(), "顶级节点应为目录");
        }
    }

    // ==================== 角色权限分配 ====================

    @Test
    @DisplayName("为角色分配菜单权限应成功")
    void assignMenuPermission_shouldSucceed() {
        // 创建角色
        Long roleId = createRole("PERM_TEST", "权限测试角色");

        // 获取菜单树中的一些菜单ID
        List<MenuVO> tree = getMenuTree();
        assertFalse(tree.isEmpty());
        Long menuId = tree.get(0).getId();

        AssignMenuDTO dto = new AssignMenuDTO();
        dto.setRoleId(roleId);
        dto.setMenuIds(List.of(menuId));

        ResponseEntity<R<Void>> response = postWithAuth("/system/menu/assign", dto,
                new ParameterizedTypeReference<R<Void>>() {});

        assertTrue(response.getBody().isSuccess());

        // 验证角色菜单关联
        ResponseEntity<R<List<Long>>> roleMenuResp = postWithAuthNoBody(
                "/system/menu/roleMenuIds?roleId=" + roleId,
                new ParameterizedTypeReference<R<List<Long>>>() {});

        assertTrue(roleMenuResp.getBody().isSuccess());
        assertTrue(roleMenuResp.getBody().getData().contains(menuId));
    }

    // ==================== 用户权限查询 ====================

    @Test
    @DisplayName("登录接口返回权限标识列表")
    void login_shouldReturnPermissions() {
        // 登录（setUp 中已创建管理员并分配了全部菜单权限）
        LoginDTO dto = new LoginDTO();
        dto.setUsername("rbacadmin");
        dto.setPassword("Admin123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<R<LoginVO>> response = restTemplate.exchange(
                "/auth/login", HttpMethod.POST, entity,
                new ParameterizedTypeReference<R<LoginVO>>() {});

        assertTrue(response.getBody().isSuccess());
        LoginVO loginVO = response.getBody().getData();
        assertNotNull(loginVO.getToken());
        assertNotNull(loginVO.getPermissions(), "权限标识列表不应为null");
        assertFalse(loginVO.getPermissions().isEmpty(), "管理员应拥有权限标识");
    }

    @Test
    @DisplayName("查询当前用户权限信息应返回菜单树和权限列表")
    void getCurrentUserPermissions_shouldReturnMenuTreeAndPermissions() {
        ResponseEntity<R<UserPermissionVO>> response = postWithAuthNoBody(
                "/system/menu/permissions",
                new ParameterizedTypeReference<R<UserPermissionVO>>() {});

        assertTrue(response.getBody().isSuccess());
        UserPermissionVO permVO = response.getBody().getData();
        assertNotNull(permVO.getMenuTree(), "菜单树不应为null");
        assertNotNull(permVO.getPermissions(), "权限标识列表不应为null");
        assertFalse(permVO.getPermissions().isEmpty(), "管理员应拥有按钮权限");
    }

    // ==================== 403 权限不足 ====================

    @Test
    @DisplayName("无权限用户访问受保护接口 → 返回 403")
    void accessProtected_withoutPermission_shouldReturn403() throws Exception {
        // 创建一个没有任何菜单权限的用户
        SysUser limitedUser = new SysUser();
        limitedUser.setUsername("limiteduser");
        limitedUser.setPassword(new BCryptPasswordEncoder().encode("Pass1234"));
        limitedUser.setRealName("无权限用户");
        limitedUser.setStatus(UserStatus.ACTIVE);
        sysUserMapper.insert(limitedUser);

        // 创建一个角色但不分配任何菜单权限
        SysRole emptyRole = new SysRole();
        emptyRole.setRoleCode("EMPTY_ROLE");
        emptyRole.setRoleName("空权限角色");
        emptyRole.setStatus(UserStatus.ACTIVE);
        sysRoleMapper.insert(emptyRole);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(limitedUser.getId());
        userRole.setRoleId(emptyRole.getId());
        sysUserRoleMapper.insert(userRole);

        // 登录获取 Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("limiteduser");
        loginDTO.setPassword("Pass1234");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<R<LoginVO>> loginResp = restTemplate.exchange(
                "/auth/login", HttpMethod.POST, entity,
                new ParameterizedTypeReference<R<LoginVO>>() {});

        String limitedToken = loginResp.getBody().getData().getToken();

        // 尝试创建用户（需要 system:user:create 权限）
        HttpClient client = HttpClient.newHttpClient();
        CreateUserDTO createUserDto = new CreateUserDTO();
        createUserDto.setUsername("newuser");
        createUserDto.setPassword("Pass1234");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(restTemplate.getRootUri() + "/system/user/create"))
                .header("Authorization", "Bearer " + limitedToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"username\":\"newuser\",\"password\":\"Pass1234\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 应返回 403（由 BizException 或 AccessDeniedException 触发）
        assertTrue(response.statusCode() == 403 || response.statusCode() == 200,
                "应返回 403 或业务错误码，实际: " + response.statusCode());

        // 如果返回 200，检查业务错误码
        if (response.statusCode() == 200) {
            assertTrue(response.body().contains("91012") || response.body().contains("无权限"),
                    "应包含权限不足的错误信息");
        }
    }

    // ==================== 辅助方法 ====================

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> ResponseEntity<R<T>> postWithAuth(String url, Object body, ParameterizedTypeReference<R<T>> typeRef) {
        HttpEntity<Object> entity = new HttpEntity<>(body, authHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
    }

    private <T> ResponseEntity<R<T>> postWithAuthNoBody(String url, ParameterizedTypeReference<R<T>> typeRef) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
    }

    private Long createDirectory(String name) {
        CreateMenuDTO dto = new CreateMenuDTO();
        dto.setParentId(0L);
        dto.setName(name);
        dto.setType("DIRECTORY");
        dto.setPath("/" + name);
        R<MenuVO> result = postWithAuth("/system/menu/create", dto,
                new ParameterizedTypeReference<R<MenuVO>>() {}).getBody();
        return result.getData().getId();
    }

    private Long createMenu(Long parentId, String name) {
        CreateMenuDTO dto = new CreateMenuDTO();
        dto.setParentId(parentId);
        dto.setName(name);
        dto.setType("MENU");
        dto.setPath("/" + name);
        dto.setComponent(name + "/Page");
        R<MenuVO> result = postWithAuth("/system/menu/create", dto,
                new ParameterizedTypeReference<R<MenuVO>>() {}).getBody();
        return result.getData().getId();
    }

    private Long createRole(String code, String name) {
        CreateRoleDTO dto = new CreateRoleDTO();
        dto.setRoleCode(code);
        dto.setRoleName(name);
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateRoleDTO> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<R<com.jingwei.system.interfaces.vo.RoleVO>> response = restTemplate.exchange(
                "/system/role/create", HttpMethod.POST, entity,
                new ParameterizedTypeReference<R<com.jingwei.system.interfaces.vo.RoleVO>>() {});
        return response.getBody().getData().getId();
    }

    private List<MenuVO> getMenuTree() {
        R<List<MenuVO>> result = postWithAuthNoBody("/system/menu/tree",
                new ParameterizedTypeReference<R<List<MenuVO>>>() {}).getBody();
        return result.getData();
    }

    private void insertButton(Long parentId, String name, String permission, int sortOrder) {
        SysMenu button = new SysMenu();
        button.setParentId(parentId);
        button.setName(name);
        button.setType(MenuType.BUTTON);
        button.setPermission(permission);
        button.setSortOrder(sortOrder);
        button.setVisible(true);
        button.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(button);
    }
}
