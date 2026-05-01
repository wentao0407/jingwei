package com.jingwei.system.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.system.domain.model.SysRole;

/**
 * 角色仓库接口
 * <p>
 * 定义角色数据的持久化操作，由 infrastructure 层实现。
 * </p>
 *
 * @author JingWei
 */
public interface SysRoleRepository {

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色实体，不存在返回 null
     */
    SysRole selectById(Long id);

    /**
     * 分页查询角色
     *
     * @param page    分页参数
     * @param keyword 搜索关键词（角色编码/角色名称）
     * @param status  状态筛选
     * @return 分页结果
     */
    IPage<SysRole> selectPage(Page<SysRole> page, String keyword, String status);

    /**
     * 新增角色
     *
     * @param role 角色实体
     * @return 影响行数
     */
    int insert(SysRole role);

    /**
     * 更新角色
     *
     * @param role 角色实体
     * @return 影响行数
     */
    int updateById(SysRole role);

    /**
     * 检查角色编码是否已存在
     *
     * @param roleCode 角色编码
     * @return true=已存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色是否存在
     *
     * @param id 角色ID
     * @return true=存在
     */
    boolean existsById(Long id);
}
