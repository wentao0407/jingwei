package com.jingwei.system.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.AssignRoleDTO;
import com.jingwei.system.application.dto.ChangePasswordDTO;
import com.jingwei.system.application.dto.CreateUserDTO;
import com.jingwei.system.application.dto.UpdateUserDTO;
import com.jingwei.system.application.dto.UserQueryDTO;
import com.jingwei.system.application.service.SystemApplicationService;
import com.jingwei.system.interfaces.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 Controller
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
public class UserController {

    private final SystemApplicationService systemApplicationService;

    /**
     * 创建用户
     *
     * @param dto 创建用户请求
     * @return 用户VO
     */
    @PostMapping("/system/user/create")
    @RequirePermission("system:user:create")
    public R<UserVO> createUser(@Valid @RequestBody CreateUserDTO dto) {
        return R.ok(systemApplicationService.createUser(dto));
    }

    /**
     * 更新用户
     *
     * @param userId 用户ID
     * @param dto    更新用户请求
     * @return 用户VO
     */
    @PostMapping("/system/user/update")
    @RequirePermission("system:user:update")
    public R<UserVO> updateUser(@RequestParam Long userId, @Valid @RequestBody UpdateUserDTO dto) {
        return R.ok(systemApplicationService.updateUser(userId, dto));
    }

    /**
     * 停用用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/system/user/deactivate")
    @RequirePermission("system:user:deactivate")
    public R<Void> deactivateUser(@RequestParam Long userId) {
        systemApplicationService.deactivateUser(userId);
        return R.ok();
    }

    /**
     * 修改密码
     * <p>
     * 用户修改自己的密码，需验证旧密码，新密码需满足密码策略（至少8位，含大小写和数字）。
     * </p>
     *
     * @param userId 用户ID
     * @param dto    修改密码请求（旧密码+新密码）
     * @return 操作结果
     */
    @PostMapping("/system/user/changePassword")
    public R<Void> changePassword(@RequestParam Long userId, @Valid @RequestBody ChangePasswordDTO dto) {
        systemApplicationService.changePassword(userId, dto);
        return R.ok();
    }

    /**
     * 分页查询用户
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @PostMapping("/system/user/page")
    public R<IPage<UserVO>> pageUser(@RequestBody UserQueryDTO dto) {
        return R.ok(systemApplicationService.pageUser(dto));
    }

    /**
     * 查询用户详情
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    @PostMapping("/system/user/detail")
    public R<UserVO> getUser(@RequestParam Long userId) {
        return R.ok(systemApplicationService.getUser(userId));
    }

    /**
     * 分配角色
     *
     * @param userId 用户ID
     * @param dto    分配角色请求
     * @return 操作结果
     */
    @PostMapping("/system/user/assignRoles")
    @RequirePermission("system:user:assignRole")
    public R<Void> assignRoles(@RequestParam Long userId, @Valid @RequestBody AssignRoleDTO dto) {
        systemApplicationService.assignRoles(userId, dto);
        return R.ok();
    }
}
