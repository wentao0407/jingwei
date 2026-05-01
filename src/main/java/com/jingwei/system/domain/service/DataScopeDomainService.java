package com.jingwei.system.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.model.DataScope;
import com.jingwei.system.domain.repository.DataScopeRepository;
import com.jingwei.system.domain.repository.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据权限领域服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeDomainService {

    private final DataScopeRepository dataScopeRepository;
    private final SysRoleRepository sysRoleRepository;

    /**
     * 为角色配置数据权限（全量替换）
     * <p>
     * 先删除该角色的所有数据权限规则，再批量插入新规则。
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void configureDataScope(Long roleId, List<DataScope> scopes) {
        if (!sysRoleRepository.existsById(roleId)) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "角色不存在");
        }

        // 先删除旧规则
        dataScopeRepository.deleteByRoleId(roleId);

        // 再插入新规则
        for (DataScope scope : scopes) {
            scope.setRoleId(roleId);
            dataScopeRepository.insert(scope);
        }

        log.info("配置数据权限: roleId={}, 规则数={}", roleId, scopes.size());
    }

    /**
     * 查询角色的数据权限规则
     */
    public List<DataScope> getByRoleId(Long roleId) {
        return dataScopeRepository.selectByRoleId(roleId);
    }
}
