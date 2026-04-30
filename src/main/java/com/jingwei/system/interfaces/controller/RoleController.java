package com.jingwei.system.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.CreateRoleDTO;
import com.jingwei.system.application.dto.RoleQueryDTO;
import com.jingwei.system.application.dto.UpdateRoleDTO;
import com.jingwei.system.application.service.SystemApplicationService;
import com.jingwei.system.interfaces.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理 Controller
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
public class RoleController {

    private final SystemApplicationService systemApplicationService;

    /**
     * 创建角色
     *
     * @param dto 创建角色请求
     * @return 角色VO
     */
    @PostMapping("/system/role/create")
    @RequirePermission("system:role:create")
    public R<RoleVO> createRole(@Valid @RequestBody CreateRoleDTO dto) {
        return R.ok(systemApplicationService.createRole(dto));
    }

    /**
     * 更新角色
     *
     * @param roleId 角色ID
     * @param dto    更新角色请求
     * @return 角色VO
     */
    @PostMapping("/system/role/update")
    @RequirePermission("system:role:update")
    public R<RoleVO> updateRole(@RequestParam Long roleId, @Valid @RequestBody UpdateRoleDTO dto) {
        return R.ok(systemApplicationService.updateRole(roleId, dto));
    }

    /**
     * 分页查询角色
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @PostMapping("/system/role/page")
    public R<IPage<RoleVO>> pageRole(@RequestBody RoleQueryDTO dto) {
        return R.ok(systemApplicationService.pageRole(dto));
    }
}
