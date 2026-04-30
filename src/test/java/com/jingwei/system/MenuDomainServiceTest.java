package com.jingwei.system;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.application.dto.CreateMenuDTO;
import com.jingwei.system.application.dto.UpdateMenuDTO;
import com.jingwei.system.domain.model.MenuType;
import com.jingwei.system.domain.model.SysMenu;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysMenuRepository;
import com.jingwei.system.domain.repository.SysRoleMenuRepository;
import com.jingwei.system.domain.service.MenuDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MenuDomainService 单元测试
 * <p>
 * 测试菜单领域服务的核心业务规则：
 * <ul>
 *   <li>按钮类型必须有 permission 标识</li>
 *   <li>permission 标识不能重复</li>
 *   <li>菜单层级校验：目录→菜单→按钮</li>
 *   <li>删除菜单时校验子菜单和角色引用</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class MenuDomainServiceTest {

    @Mock
    private SysMenuRepository sysMenuRepository;

    @Mock
    private SysRoleMenuRepository sysRoleMenuRepository;

    @InjectMocks
    private MenuDomainService menuDomainService;

    // ==================== 创建菜单 ====================

    @Test
    @DisplayName("创建按钮类型菜单 — 没有 permission 标识应抛异常")
    void createMenu_buttonWithoutPermission_shouldThrow() {
        SysMenu menu = new SysMenu();
        menu.setParentId(110L);
        menu.setName("测试按钮");
        menu.setType(MenuType.BUTTON);
        menu.setPermission("");  // 空字符串 — 按钮类型校验在父菜单查找之前，不需要 stub parent

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.createMenu(menu));
        assertEquals(ErrorCode.MENU_BUTTON_PERMISSION_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建按钮类型菜单 — permission 标识重复应抛异常")
    void createMenu_duplicatePermission_shouldThrow() {
        SysMenu menu = new SysMenu();
        menu.setParentId(110L);
        menu.setName("测试按钮");
        menu.setType(MenuType.BUTTON);
        menu.setPermission("system:user:create");

        // permission 重复校验在父菜单查找之前，不需要 stub parent
        when(sysMenuRepository.existsByPermission("system:user:create")).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.createMenu(menu));
        assertEquals(ErrorCode.MENU_PERMISSION_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建菜单 — 父菜单不存在应抛异常")
    void createMenu_parentNotFound_shouldThrow() {
        SysMenu menu = new SysMenu();
        menu.setParentId(999L);
        menu.setName("测试菜单");
        menu.setType(MenuType.MENU);
        when(sysMenuRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.createMenu(menu));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建菜单 — 目录下放按钮应抛异常")
    void createMenu_directoryWithButtonChild_shouldThrow() {
        SysMenu menu = new SysMenu();
        menu.setParentId(100L);
        menu.setName("测试按钮");
        menu.setType(MenuType.BUTTON);
        menu.setPermission("test:perm");

        SysMenu parent = new SysMenu();
        parent.setId(100L);
        parent.setType(MenuType.DIRECTORY);
        when(sysMenuRepository.selectById(100L)).thenReturn(parent);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.createMenu(menu));
        assertEquals(ErrorCode.MENU_HIERARCHY_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建菜单 — 菜单下放目录应抛异常")
    void createMenu_menuWithDirectoryChild_shouldThrow() {
        SysMenu menu = new SysMenu();
        menu.setParentId(110L);
        menu.setName("测试目录");
        menu.setType(MenuType.DIRECTORY);

        SysMenu parent = new SysMenu();
        parent.setId(110L);
        parent.setType(MenuType.MENU);
        when(sysMenuRepository.selectById(110L)).thenReturn(parent);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.createMenu(menu));
        assertEquals(ErrorCode.MENU_HIERARCHY_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建菜单 — 按钮下创建子项应抛异常")
    void createMenu_buttonWithChild_shouldThrow() {
        SysMenu menu = new SysMenu();
        menu.setParentId(111L);
        menu.setName("测试按钮");
        menu.setType(MenuType.BUTTON);
        menu.setPermission("test:perm2");

        SysMenu parent = new SysMenu();
        parent.setId(111L);
        parent.setType(MenuType.BUTTON);
        when(sysMenuRepository.selectById(111L)).thenReturn(parent);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.createMenu(menu));
        assertEquals(ErrorCode.MENU_HIERARCHY_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建顶级菜单 — 非目录类型应抛异常")
    void createMenu_topLevelNotDirectory_shouldThrow() {
        SysMenu menu = new SysMenu();
        menu.setParentId(0L);
        menu.setName("测试菜单");
        menu.setType(MenuType.MENU);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.createMenu(menu));
        assertEquals(ErrorCode.MENU_HIERARCHY_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建菜单 — 目录下放菜单应成功")
    void createMenu_directoryWithMenuChild_shouldSucceed() {
        SysMenu menu = new SysMenu();
        menu.setParentId(100L);
        menu.setName("用户管理");
        menu.setType(MenuType.MENU);

        SysMenu parent = new SysMenu();
        parent.setId(100L);
        parent.setType(MenuType.DIRECTORY);
        when(sysMenuRepository.selectById(100L)).thenReturn(parent);
        when(sysMenuRepository.insert(any())).thenReturn(1);

        SysMenu result = menuDomainService.createMenu(menu);
        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertTrue(result.getVisible());
        verify(sysMenuRepository).insert(menu);
    }

    @Test
    @DisplayName("创建菜单 — 菜单下放按钮应成功")
    void createMenu_menuWithButtonChild_shouldSucceed() {
        SysMenu menu = new SysMenu();
        menu.setParentId(110L);
        menu.setName("创建用户");
        menu.setType(MenuType.BUTTON);
        menu.setPermission("system:user:create");

        SysMenu parent = new SysMenu();
        parent.setId(110L);
        parent.setType(MenuType.MENU);
        when(sysMenuRepository.selectById(110L)).thenReturn(parent);
        when(sysMenuRepository.existsByPermission("system:user:create")).thenReturn(false);
        when(sysMenuRepository.insert(any())).thenReturn(1);

        SysMenu result = menuDomainService.createMenu(menu);
        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(sysMenuRepository).insert(menu);
    }

    // ==================== 更新菜单 ====================

    @Test
    @DisplayName("更新菜单 — 菜单不存在应抛异常")
    void updateMenu_notFound_shouldThrow() {
        UpdateMenuDTO dto = new UpdateMenuDTO();
        dto.setName("新名称");
        when(sysMenuRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.updateMenu(999L, dto));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("更新菜单 — 修改 permission 为已存在的值应抛异常")
    void updateMenu_duplicatePermission_shouldThrow() {
        SysMenu existing = new SysMenu();
        existing.setId(111L);
        existing.setPermission("old:perm");
        when(sysMenuRepository.selectById(111L)).thenReturn(existing);
        when(sysMenuRepository.existsByPermission("new:perm")).thenReturn(true);

        UpdateMenuDTO dto = new UpdateMenuDTO();
        dto.setPermission("new:perm");

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.updateMenu(111L, dto));
        assertEquals(ErrorCode.MENU_PERMISSION_DUPLICATE.getCode(), ex.getCode());
    }

    // ==================== 删除菜单 ====================

    @Test
    @DisplayName("删除菜单 — 存在子菜单应抛异常")
    void deleteMenu_hasChildren_shouldThrow() {
        SysMenu existing = new SysMenu();
        existing.setId(100L);
        when(sysMenuRepository.selectById(100L)).thenReturn(existing);
        when(sysMenuRepository.countByParentId(100L)).thenReturn(3L);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.deleteMenu(100L));
        assertEquals(ErrorCode.MENU_HAS_CHILDREN.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("删除菜单 — 被角色引用应抛异常")
    void deleteMenu_assignedToRole_shouldThrow() {
        SysMenu existing = new SysMenu();
        existing.setId(111L);
        when(sysMenuRepository.selectById(111L)).thenReturn(existing);
        when(sysMenuRepository.countByParentId(111L)).thenReturn(0L);
        when(sysRoleMenuRepository.existsByMenuId(111L)).thenReturn(true);

        BizException ex = assertThrows(BizException.class, () -> menuDomainService.deleteMenu(111L));
        assertEquals(ErrorCode.MENU_ASSIGNED_TO_ROLE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("删除菜单 — 无子菜单且无角色引用应成功")
    void deleteMenu_noChildrenAndNoRoleRef_shouldSucceed() {
        SysMenu existing = new SysMenu();
        existing.setId(133L);
        existing.setName("测试删除");
        when(sysMenuRepository.selectById(133L)).thenReturn(existing);
        when(sysMenuRepository.countByParentId(133L)).thenReturn(0L);
        when(sysRoleMenuRepository.existsByMenuId(133L)).thenReturn(false);
        when(sysMenuRepository.deleteById(133L)).thenReturn(1);

        assertDoesNotThrow(() -> menuDomainService.deleteMenu(133L));
        verify(sysMenuRepository).deleteById(133L);
    }
}
