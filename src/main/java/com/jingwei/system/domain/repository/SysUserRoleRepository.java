package com.jingwei.system.domain.repository;

import com.jingwei.system.domain.model.SysUserRole;

import java.util.List;

/**
 * 用户角色关联仓库接口
 * <p>
 * 定义用户角色关联数据的持久化操作，由 infrastructure 层实现。
 * </p>
 *
 * @author JingWei
 */
public interface SysUserRoleRepository {

    /**
     * 批量新增用户角色关联
     *
     * @param userRoles 用户角色关联列表
     * @return 影响行数
     */
    int batchInsert(List<SysUserRole> userRoles);

    /**
     * 删除用户的所有角色关联
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Long userId);

    /**
     * 查询用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 查询指定角色下的用户ID列表
     * <p>
     * 审批引擎使用：根据审批配置中的角色ID，查找该角色下的所有用户，
     * 为其生成审批待办任务。
     * </p>
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByRoleId(Long roleId);
}
