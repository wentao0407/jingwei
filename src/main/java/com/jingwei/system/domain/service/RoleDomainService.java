package com.jingwei.system.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.application.dto.UpdateRoleDTO;
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
     * <p>
     * 先查出 existing 实体（携带正确的 version），将 DTO 变更字段合并到 existing 上，
     * 再用 existing 进行更新，确保乐观锁 version 条件正确。
     * </p>
     *
     * @param roleId 角色ID
     * @param dto    更新角色请求DTO
     * @return 更新后的角色实体
     */
    public SysRole updateRole(Long roleId, UpdateRoleDTO dto) {
        SysRole existing = sysRoleRepository.selectById(roleId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "角色不存在");
        }

        // 将 DTO 中非 null 字段合并到 existing，保留正确的 version 用于乐观锁
        if (dto.getRoleName() != null) {
            existing.setRoleName(dto.getRoleName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(UserStatus.valueOf(dto.getStatus()));
        }

        int rows = sysRoleRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新角色: id={}", roleId);
        return sysRoleRepository.selectById(roleId);
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
