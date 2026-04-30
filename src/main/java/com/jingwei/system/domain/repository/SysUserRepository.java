package com.jingwei.system.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.system.domain.model.SysUser;

/**
 * 用户仓库接口
 * <p>
 * 定义用户数据的持久化操作，由 infrastructure 层实现。
 * </p>
 *
 * @author JingWei
 */
public interface SysUserRepository {

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体，不存在返回 null
     */
    SysUser selectById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，不存在返回 null
     */
    SysUser selectByUsername(String username);

    /**
     * 分页查询用户
     *
     * @param page    分页参数
     * @param keyword 搜索关键词（用户名/姓名/手机号）
     * @param status  状态筛选
     * @return 分页结果
     */
    IPage<SysUser> selectPage(Page<SysUser> page, String keyword, String status);

    /**
     * 新增用户
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(SysUser user);

    /**
     * 更新用户
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int updateById(SysUser user);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return true=已存在
     */
    boolean existsByUsername(String username);
}
