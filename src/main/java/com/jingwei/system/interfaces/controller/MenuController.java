package com.jingwei.system.interfaces.controller;

import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.AssignMenuDTO;
import com.jingwei.system.application.dto.CreateMenuDTO;
import com.jingwei.system.application.dto.UpdateMenuDTO;
import com.jingwei.system.application.service.MenuApplicationService;
import com.jingwei.system.interfaces.vo.MenuVO;
import com.jingwei.system.interfaces.vo.UserPermissionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理 Controller
 * <p>
 * 只做参数校验和调用 Service，不含业务逻辑。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuApplicationService menuApplicationService;

    /**
     * 创建菜单
     *
     * @param dto 创建菜单请求
     * @return 菜单VO
     */
    @PostMapping("/system/menu/create")
    @RequirePermission("system:menu:create")
    public R<MenuVO> createMenu(@Valid @RequestBody CreateMenuDTO dto) {
        return R.ok(menuApplicationService.createMenu(dto));
    }

    /**
     * 更新菜单
     *
     * @param menuId 菜单ID
     * @param dto    更新菜单请求
     * @return 菜单VO
     */
    @PostMapping("/system/menu/update")
    @RequirePermission("system:menu:update")
    public R<MenuVO> updateMenu(@RequestParam Long menuId, @Valid @RequestBody UpdateMenuDTO dto) {
        return R.ok(menuApplicationService.updateMenu(menuId, dto));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 操作结果
     */
    @PostMapping("/system/menu/delete")
    @RequirePermission("system:menu:delete")
    public R<Void> deleteMenu(@RequestParam Long menuId) {
        menuApplicationService.deleteMenu(menuId);
        return R.ok();
    }

    /**
     * 查询完整菜单树
     *
     * @return 菜单树
     */
    @PostMapping("/system/menu/tree")
    public R<List<MenuVO>> getMenuTree() {
        return R.ok(menuApplicationService.getMenuTree());
    }

    /**
     * 为角色分配菜单权限
     *
     * @param dto 分配权限请求
     * @return 操作结果
     */
    @PostMapping("/system/menu/assign")
    @RequirePermission("system:role:assignPermission")
    public R<Void> assignMenuPermission(@Valid @RequestBody AssignMenuDTO dto) {
        menuApplicationService.assignMenuPermission(dto);
        return R.ok();
    }

    /**
     * 查询角色已分配的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @PostMapping("/system/menu/roleMenuIds")
    public R<List<Long>> getRoleMenuIds(@RequestParam Long roleId) {
        return R.ok(menuApplicationService.getRoleMenuIds(roleId));
    }

    /**
     * 查询当前登录用户的权限信息（菜单树+权限标识列表）
     *
     * @return 用户权限信息
     */
    @PostMapping("/system/menu/permissions")
    public R<UserPermissionVO> getCurrentUserPermissions() {
        return R.ok(menuApplicationService.getCurrentUserPermissions());
    }
}
