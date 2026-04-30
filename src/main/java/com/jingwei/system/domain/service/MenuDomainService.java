package com.jingwei.system.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.application.dto.UpdateMenuDTO;
import com.jingwei.system.domain.model.MenuType;
import com.jingwei.system.domain.model.SysMenu;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysMenuRepository;
import com.jingwei.system.domain.repository.SysRoleMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单领域服务
 * <p>
 * 封装菜单相关的纯业务逻辑，包括菜单树构建、菜单CRUD校验等。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuDomainService {

    private final SysMenuRepository sysMenuRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;

    /**
     * 创建菜单
     * <p>
     * 校验规则：
     * 1. 按钮类型必须有 permission 标识
     * 2. permission 标识不能重复
     * 3. 父菜单必须存在（parentId != 0 时）
     * 4. 目录只能有二级子菜单，菜单才能有按钮子节点
     * </p>
     *
     * @param menu 菜单实体
     * @return 保存后的菜单实体
     */
    public SysMenu createMenu(SysMenu menu) {
        MenuType menuType = menu.getType();

        // 按钮类型必须有 permission 标识
        if (menuType == MenuType.BUTTON) {
            if (menu.getPermission() == null || menu.getPermission().isBlank()) {
                throw new BizException(ErrorCode.MENU_BUTTON_PERMISSION_REQUIRED);
            }
            // permission 标识不能重复
            if (sysMenuRepository.existsByPermission(menu.getPermission())) {
                throw new BizException(ErrorCode.MENU_PERMISSION_DUPLICATE);
            }
        }

        // 父菜单必须存在（parentId != 0 时）
        if (menu.getParentId() != 0) {
            SysMenu parent = sysMenuRepository.selectById(menu.getParentId());
            if (parent == null) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND, "父菜单不存在");
            }

            // 校验菜单层级：目录下只能放菜单，菜单下只能放按钮
            validateMenuHierarchy(parent.getType(), menuType);
        } else {
            // 顶级只能是目录
            if (menuType != MenuType.DIRECTORY) {
                throw new BizException(ErrorCode.MENU_HIERARCHY_INVALID, "顶级菜单只能是目录类型");
            }
        }

        // 设置默认值
        menu.setStatus(UserStatus.ACTIVE);
        if (menu.getVisible() == null) {
            menu.setVisible(true);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }

        sysMenuRepository.insert(menu);
        log.info("创建菜单: name={}, type={}, id={}", menu.getName(), menu.getType(), menu.getId());
        return menu;
    }

    /**
     * 更新菜单
     * <p>
     * 先查出 existing 实体（携带正确的 version），将 DTO 变更字段合并到 existing 上，
     * 再用 existing 进行更新，确保乐观锁 version 条件正确。
     * </p>
     *
     * @param menuId 菜单ID
     * @param dto    更新菜单请求DTO
     * @return 更新后的菜单实体
     */
    public SysMenu updateMenu(Long menuId, UpdateMenuDTO dto) {
        SysMenu existing = sysMenuRepository.selectById(menuId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "菜单不存在");
        }

        // 将 DTO 中非 null 字段合并到 existing，保留正确的 version 用于乐观锁
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getPath() != null) {
            existing.setPath(dto.getPath());
        }
        if (dto.getComponent() != null) {
            existing.setComponent(dto.getComponent());
        }
        if (dto.getPermission() != null) {
            // 修改 permission 时校验唯一性（排除自身）
            if (!dto.getPermission().equals(existing.getPermission())
                    && !dto.getPermission().isBlank()
                    && sysMenuRepository.existsByPermission(dto.getPermission())) {
                throw new BizException(ErrorCode.MENU_PERMISSION_DUPLICATE);
            }
            existing.setPermission(dto.getPermission());
        }
        if (dto.getIcon() != null) {
            existing.setIcon(dto.getIcon());
        }
        if (dto.getSortOrder() != null) {
            existing.setSortOrder(dto.getSortOrder());
        }
        if (dto.getVisible() != null) {
            existing.setVisible(dto.getVisible());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(UserStatus.valueOf(dto.getStatus()));
        }

        int rows = sysMenuRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新菜单: id={}", menuId);
        return sysMenuRepository.selectById(menuId);
    }

    /**
     * 删除菜单
     * <p>
     * 校验规则：
     * 1. 存在子菜单时不可删除
     * 2. 被角色引用时不可删除
     * </p>
     *
     * @param menuId 菜单ID
     */
    public void deleteMenu(Long menuId) {
        SysMenu existing = sysMenuRepository.selectById(menuId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "菜单不存在");
        }

        // 存在子菜单时不可删除
        long childCount = sysMenuRepository.countByParentId(menuId);
        if (childCount > 0) {
            throw new BizException(ErrorCode.MENU_HAS_CHILDREN);
        }

        // 被角色引用时不可删除
        if (sysRoleMenuRepository.existsByMenuId(menuId)) {
            throw new BizException(ErrorCode.MENU_ASSIGNED_TO_ROLE);
        }

        sysMenuRepository.deleteById(menuId);
        log.info("删除菜单: id={}, name={}", menuId, existing.getName());
    }

    /**
     * 查询所有菜单（用于管理端构建完整菜单树）
     *
     * @return 所有菜单列表
     */
    public List<SysMenu> listAllMenus() {
        return sysMenuRepository.selectAll();
    }

    /**
     * 根据角色ID列表查询有权限的菜单
     *
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    public List<SysMenu> listMenusByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return sysMenuRepository.selectByRoleIds(roleIds);
    }

    /**
     * 校验菜单层级关系
     * <p>
     * 规则：目录下只能放菜单，菜单下只能放按钮。
     * 这样保证严格的三级结构：目录→菜单→按钮。
     * </p>
     *
     * @param parentType 父菜单类型
     * @param childType  子菜单类型
     */
    private void validateMenuHierarchy(MenuType parentType, MenuType childType) {
        // 目录下只能放菜单
        if (parentType == MenuType.DIRECTORY && childType != MenuType.MENU) {
            throw new BizException(ErrorCode.MENU_HIERARCHY_INVALID, "目录下只能创建菜单");
        }
        // 菜单下只能放按钮
        if (parentType == MenuType.MENU && childType != MenuType.BUTTON) {
            throw new BizException(ErrorCode.MENU_HIERARCHY_INVALID, "菜单下只能创建按钮");
        }
        // 按钮下不能再创建子项
        if (parentType == MenuType.BUTTON) {
            throw new BizException(ErrorCode.MENU_HIERARCHY_INVALID, "按钮下不能创建子项");
        }
    }
}
