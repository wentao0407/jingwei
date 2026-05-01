package com.jingwei.system.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.application.dto.*;
import com.jingwei.system.domain.model.*;
import com.jingwei.system.domain.repository.SysRoleRepository;
import com.jingwei.system.domain.service.AuditLogDomainService;
import com.jingwei.system.domain.service.DataScopeDomainService;
import com.jingwei.system.domain.service.SysConfigDomainService;
import com.jingwei.system.interfaces.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统管理扩展应用服务
 * <p>
 * 覆盖数据权限、操作日志、系统配置三个子功能的编排。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemExtApplicationService {

    private final DataScopeDomainService dataScopeDomainService;
    private final AuditLogDomainService auditLogDomainService;
    private final SysConfigDomainService sysConfigDomainService;

    // ==================== 数据权限 ====================

    @Transactional(rollbackFor = Exception.class)
    public void configureDataScope(Long roleId, BatchDataScopeDTO dto) {
        List<DataScope> scopes = dto.getScopes().stream().map(s -> {
            DataScope scope = new DataScope();
            scope.setScopeType(s.getScopeType());
            scope.setScopeValue(s.getScopeValue());
            return scope;
        }).toList();
        dataScopeDomainService.configureDataScope(roleId, scopes);
    }

    public List<DataScopeVO> getDataScopeByRoleId(Long roleId) {
        List<DataScope> scopes = dataScopeDomainService.getByRoleId(roleId);
        return scopes.stream().map(this::toDataScopeVO).toList();
    }

    // ==================== 操作日志 ====================

    public IPage<AuditLogVO> pageQueryAuditLog(AuditLogQueryDTO dto) {
        Page<AuditLog> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<AuditLog> logPage = auditLogDomainService.getAuditLogRepository()
                .selectPage(page, dto.getUserId(), dto.getModule(),
                        dto.getOperationType(), dto.getStartTime(), dto.getEndTime(), dto.getKeyword());
        return logPage.convert(this::toAuditLogVO);
    }

    // ==================== 系统配置 ====================

    public SysConfigVO createConfig(CreateSysConfigDTO dto) {
        SysConfig config = new SysConfig();
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigGroup(dto.getConfigGroup() != null ? dto.getConfigGroup() : "DEFAULT");
        config.setDescription(dto.getDescription());
        config.setNeedRestart(dto.getNeedRestart() != null ? dto.getNeedRestart() : false);
        config.setRemark("");

        SysConfig saved = sysConfigDomainService.createConfig(config);
        return toSysConfigVO(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysConfigVO updateConfig(Long configId, UpdateSysConfigDTO dto) {
        SysConfig config = new SysConfig();
        if (dto.getConfigValue() != null) {
            config.setConfigValue(dto.getConfigValue());
        }
        config.setDescription(dto.getDescription());
        config.setNeedRestart(dto.getNeedRestart());
        config.setRemark(dto.getRemark());

        SysConfig updated = sysConfigDomainService.updateConfig(configId, config);
        return toSysConfigVO(updated);
    }

    public SysConfigVO getByConfigKey(String configKey) {
        SysConfig config = sysConfigDomainService.getByConfigKey(configKey);
        return toSysConfigVO(config);
    }

    public List<SysConfigVO> listByGroup(String configGroup) {
        List<SysConfig> configs = sysConfigDomainService.listByGroup(configGroup);
        return configs.stream().map(this::toSysConfigVO).toList();
    }

    public List<SysConfigVO> listAllConfigs() {
        List<SysConfig> configs = sysConfigDomainService.listAll();
        return configs.stream().map(this::toSysConfigVO).toList();
    }

    // ==================== 转换方法 ====================

    private DataScopeVO toDataScopeVO(DataScope scope) {
        DataScopeVO vo = new DataScopeVO();
        vo.setId(scope.getId());
        vo.setRoleId(scope.getRoleId());
        vo.setScopeType(scope.getScopeType());
        vo.setScopeValue(scope.getScopeValue());
        return vo;
    }

    private AuditLogVO toAuditLogVO(AuditLog auditLog) {
        AuditLogVO vo = new AuditLogVO();
        vo.setId(auditLog.getId());
        vo.setUserId(auditLog.getUserId());
        vo.setUsername(auditLog.getUsername());
        vo.setOperationType(auditLog.getOperationType());
        vo.setModule(auditLog.getModule());
        vo.setDescription(auditLog.getDescription());
        vo.setOldValue(auditLog.getOldValue());
        vo.setNewValue(auditLog.getNewValue());
        vo.setIpAddress(auditLog.getIpAddress());
        vo.setCreatedAt(auditLog.getCreatedAt());
        return vo;
    }

    private SysConfigVO toSysConfigVO(SysConfig config) {
        SysConfigVO vo = new SysConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigGroup(config.getConfigGroup());
        vo.setDescription(config.getDescription());
        vo.setNeedRestart(config.getNeedRestart());
        vo.setRemark(config.getRemark());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}
