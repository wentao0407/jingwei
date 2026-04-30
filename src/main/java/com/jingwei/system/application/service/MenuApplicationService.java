package com.jingwei.system.application.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.system.application.dto.AssignMenuDTO;
import com.jingwei.system.application.dto.CreateMenuDTO;
import com.jingwei.system.application.dto.UpdateMenuDTO;
import com.jingwei.system.domain.model.MenuType;
import com.jingwei.system.domain.model.SysMenu;
import com.jingwei.system.domain.model.SysRoleMenu;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysMenuRepository;
import com.jingwei.system.domain.repository.SysRoleMenuRepository;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import com.jingwei.system.domain.service.MenuDomainService;
import com.jingwei.system.interfaces.vo.MenuVO;
import com.jingwei.system.interfaces.vo.UserPermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单应用服务
 * <p>
 * 负责菜单管理、角色权限分配、用户权限查询的业务编排，持有事务边界。
 * Controller 只调用此服务，不直接调用 DomainService。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuApplicationService {

    private final MenuDomainService menuDomainService;
    private final SysMenuRepository sysMenuRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    /**
     * 创建菜单
     *
     * @param dto 创建菜单请求
     * @return 菜单VO
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuVO createMenu(CreateMenuDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setParentId(dto.getParentId());
        menu.setName(dto.getName());
        menu.setType(MenuType.valueOf(dto.getType()));
        menu.setPath(dto.getPath() != null ? dto.getPath() : "");
        menu.setComponent(dto.getComponent() != null ? dto.getComponent() : "");
        menu.setPermission(dto.getPermission() != null ? dto.getPermission() : "");
        menu.setIcon(dto.getIcon() != null ? dto.getIcon() : "");
        menu.setSortOrder(dto.getSortOrder());
        menu.setVisible(dto.getVisible());

        menuDomainService.createMenu(menu);
        return toMenuVO(menu);
    }

    /**
     * 更新菜单
     *
     * @param menuId 菜单ID
     * @param dto    更新菜单请求
     * @return 菜单VO
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuVO updateMenu(Long menuId, UpdateMenuDTO dto) {
        SysMenu updated = menuDomainService.updateMenu(menuId, dto);
        return toMenuVO(updated);
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        menuDomainService.deleteMenu(menuId);
    }

    /**
     * 查询完整菜单树（管理端使用，包含所有菜单）
     *
     * @return 菜单树
     */
    public List<MenuVO> getMenuTree() {
        List<SysMenu> allMenus = menuDomainService.listAllMenus();
        return buildMenuTree(allMenus);
    }

    /**
     * 为角色分配菜单权限
     * <p>
     * 先删除原有角色菜单关联，再批量插入新关联。
     * </p>
     *
     * @param dto 分配权限请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenuPermission(AssignMenuDTO dto) {
        Long roleId = dto.getRoleId();

        // 先删除旧关联
        sysRoleMenuRepository.deleteByRoleId(roleId);

        // 批量插入新关联
        if (!dto.getMenuIds().isEmpty()) {
            List<SysRoleMenu> roleMenus = dto.getMenuIds().stream()
                    .map(menuId -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(menuId);
                        return rm;
                    })
                    .toList();
            sysRoleMenuRepository.batchInsert(roleMenus);
        }

        log.info("分配菜单权限: roleId={}, menuCount={}", roleId, dto.getMenuIds().size());
    }

    /**
     * 查询角色已分配的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMenuRepository.selectMenuIdsByRoleId(roleId);
    }

    /**
     * 查询当前登录用户的权限信息（菜单树+权限标识列表）
     * <p>
     * 登录后前端调用此接口获取用户可见菜单和按钮权限。
     * 一个用户可分配多个角色，权限取并集。
     * </p>
     *
     * @return 用户权限信息
     */
    public UserPermissionVO getCurrentUserPermissions() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        // 查询用户角色ID列表
        List<Long> roleIds = sysUserRoleRepository.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            // 无角色则返回空权限
            UserPermissionVO vo = new UserPermissionVO();
            vo.setMenuTree(List.of());
            vo.setPermissions(List.of());
            return vo;
        }

        // 查询有权限的菜单
        List<SysMenu> menus = menuDomainService.listMenusByRoleIds(roleIds);

        // 构建菜单树（仅包含目录和菜单，不含按钮）
        List<MenuVO> menuTree = buildAuthorizedMenuTree(menus);

        // 提取所有按钮权限标识
        List<String> permissions = menus.stream()
                .filter(m -> m.getType() == MenuType.BUTTON)
                .map(SysMenu::getPermission)
                .filter(p -> p != null && !p.isBlank())
                .toList();

        UserPermissionVO vo = new UserPermissionVO();
        vo.setMenuTree(menuTree);
        vo.setPermissions(permissions);
        return vo;
    }

    /**
     * 根据用户ID查询授权菜单树和权限标识列表
     * <p>
     * 供登录接口使用，在登录成功后一次性返回菜单树和权限标识，
     * 避免前端登录后再发一次请求获取权限。
     * 逻辑与 {@link #getCurrentUserPermissions()} 一致，但接受 userId 参数，
     * 不依赖 UserContext（登录时还未设置上下文）。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户权限信息（菜单树 + 权限标识列表）
     */
    public UserPermissionVO getUserPermissionsByUserId(Long userId) {
        // 查询用户角色ID列表
        List<Long> roleIds = sysUserRoleRepository.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            UserPermissionVO vo = new UserPermissionVO();
            vo.setMenuTree(List.of());
            vo.setPermissions(List.of());
            return vo;
        }

        // 查询有权限的菜单
        List<SysMenu> menus = menuDomainService.listMenusByRoleIds(roleIds);

        // 构建菜单树（仅包含目录和菜单，不含按钮）
        List<MenuVO> menuTree = buildAuthorizedMenuTree(menus);

        // 提取所有按钮权限标识
        List<String> permissions = menus.stream()
                .filter(m -> m.getType() == MenuType.BUTTON)
                .map(SysMenu::getPermission)
                .filter(p -> p != null && !p.isBlank())
                .toList();

        UserPermissionVO vo = new UserPermissionVO();
        vo.setMenuTree(menuTree);
        vo.setPermissions(permissions);
        return vo;
    }

    /**
     * 根据用户ID查询权限标识列表
     * <p>
     * 供 JwtAuthenticationFilter 使用，在认证时加载用户权限到 SecurityContext。
     * </p>
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    public List<String> getPermissionIdentifiersByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleRepository.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<SysMenu> menus = menuDomainService.listMenusByRoleIds(roleIds);
        return menus.stream()
                .filter(m -> m.getType() == MenuType.BUTTON)
                .map(SysMenu::getPermission)
                .filter(p -> p != null && !p.isBlank())
                .toList();
    }

    // ==================== 转换方法 ====================

    /**
     * 菜单实体转VO
     */
    private MenuVO toMenuVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setType(menu.getType().name());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setPermission(menu.getPermission());
        vo.setIcon(menu.getIcon());
        vo.setSortOrder(menu.getSortOrder());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus().name());
        vo.setCreatedAt(menu.getCreatedAt());
        vo.setUpdatedAt(menu.getUpdatedAt());
        return vo;
    }

    /**
     * 构建授权菜单树（过滤掉按钮，只保留目录+菜单）
     * <p>
     * 供登录和权限查询使用，前端只需目录和菜单节点渲染导航栏，按钮权限通过 permissions 列表控制。
     * </p>
     *
     * @param menus 用户有权限的菜单列表（含按钮）
     * @return 菜单树（仅目录+菜单，不含按钮）
     */
    private List<MenuVO> buildAuthorizedMenuTree(List<SysMenu> menus) {
        List<SysMenu> directoryAndMenu = menus.stream()
                .filter(m -> m.getType() != MenuType.BUTTON)
                .toList();
        return buildMenuTree(directoryAndMenu);
    }

    /**
     * 构建菜单树
     * <p>
     * 一次性查出所有记录，在内存中组装树形结构。
     * </p>
     *
     * @param menus 菜单列表
     * @return 菜单树（顶级节点列表）
     */
    private List<MenuVO> buildMenuTree(List<SysMenu> menus) {
        // 先转为VO列表
        List<MenuVO> allVOs = menus.stream()
                .map(this::toMenuVO)
                .toList();

        // 按 parentId 分组
        Map<Long, List<MenuVO>> parentMap = allVOs.stream()
                .collect(Collectors.groupingBy(MenuVO::getParentId));

        // 为每个节点设置子节点列表
        for (MenuVO vo : allVOs) {
            List<MenuVO> children = parentMap.get(vo.getId());
            // 只在有子节点时设置 children，避免 JSON 序列化时出现空数组
            if (children != null && !children.isEmpty()) {
                vo.setChildren(children);
            }
        }

        // 返回顶级节点（parentId=0 的节点）
        return parentMap.getOrDefault(0L, List.of());
    }
}
