package com.jingwei.system.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.model.SysRole;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 角色领域服务
 * <p>
 * 封装角色相关的纯业务逻辑。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleDomainService {

    private final SysRoleRepository sysRoleRepository;

    /**
     * 创建角色
     * <p>
     * 校验角色编码唯一性。
     * </p>
     *
     * @param role 角色实体
     * @return 保存后的角色实体
     */
    public SysRole createRole(SysRole role) {
        if (sysRoleRepository.existsByRoleCode(role.getRoleCode())) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "角色编码已存在");
        }

        role.setStatus(UserStatus.ACTIVE);
        sysRoleRepository.insert(role);
        log.info("创建角色: roleCode={}, id={}", role.getRoleCode(), role.getId());
        return role;
    }

    /**
     * 更新角色信息
     *
     * @param role 角色实体
     * @return 更新后的角色实体
     */
    public SysRole updateRole(SysRole role) {
        SysRole existing = sysRoleRepository.selectById(role.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "角色不存在");
        }

        int rows = sysRoleRepository.updateById(role);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新角色: id={}", role.getId());
        return sysRoleRepository.selectById(role.getId());
    }

    /**
     * 分页查询角色
     *
     * @param page    分页参数
     * @param keyword 搜索关键词
     * @param status  状态筛选
     * @return 分页结果
     */
    public IPage<SysRole> pageQuery(Page<SysRole> page, String keyword, String status) {
        return sysRoleRepository.selectPage(page, keyword, status);
    }
}
