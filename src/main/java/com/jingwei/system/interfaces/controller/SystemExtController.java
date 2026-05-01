package com.jingwei.system.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.domain.model.R;
import com.jingwei.system.application.dto.*;
import com.jingwei.system.application.service.SystemExtApplicationService;
import com.jingwei.system.interfaces.vo.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统管理扩展 Controller
 * <p>
 * 包含数据权限、操作日志、系统配置接口。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SystemExtController {

    private final SystemExtApplicationService systemExtApplicationService;

    // ==================== 数据权限 ====================

    /**
     * 为角色配置数据权限（全量替换）
     */
    @PostMapping("/system/data-scope/configure")
    public R<Void> configureDataScope(@RequestParam Long roleId,
                                      @Valid @RequestBody BatchDataScopeDTO dto) {
        systemExtApplicationService.configureDataScope(roleId, dto);
        return R.ok();
    }

    /**
     * 查询角色的数据权限规则
     */
    @PostMapping("/system/data-scope/query")
    public R<List<DataScopeVO>> getDataScope(@RequestParam Long roleId) {
        return R.ok(systemExtApplicationService.getDataScopeByRoleId(roleId));
    }

    // ==================== 操作日志 ====================

    /**
     * 分页查询操作日志
     */
    @PostMapping("/system/audit-log/page")
    public R<IPage<AuditLogVO>> pageQueryAuditLog(@Valid @RequestBody AuditLogQueryDTO dto) {
        return R.ok(systemExtApplicationService.pageQueryAuditLog(dto));
    }

    // ==================== 系统配置 ====================

    /**
     * 创建配置项
     */
    @PostMapping("/system/config/create")
    public R<SysConfigVO> createConfig(@Valid @RequestBody CreateSysConfigDTO dto) {
        return R.ok(systemExtApplicationService.createConfig(dto));
    }

    /**
     * 更新配置项（需填写修改原因）
     */
    @PostMapping("/system/config/update")
    public R<SysConfigVO> updateConfig(@RequestParam Long configId,
                                       @Valid @RequestBody UpdateSysConfigDTO dto) {
        return R.ok(systemExtApplicationService.updateConfig(configId, dto));
    }

    /**
     * 根据配置键查询
     */
    @PostMapping("/system/config/get")
    public R<SysConfigVO> getByConfigKey(@RequestParam String configKey) {
        return R.ok(systemExtApplicationService.getByConfigKey(configKey));
    }

    /**
     * 按分组查询配置项
     */
    @PostMapping("/system/config/list-by-group")
    public R<List<SysConfigVO>> listByGroup(@RequestParam String configGroup) {
        return R.ok(systemExtApplicationService.listByGroup(configGroup));
    }

    /**
     * 查询全部配置项
     */
    @PostMapping("/system/config/list")
    public R<List<SysConfigVO>> listAllConfigs() {
        return R.ok(systemExtApplicationService.listAllConfigs());
    }
}
