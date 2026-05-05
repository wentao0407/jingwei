package com.jingwei.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.system.domain.model.SysRole;
import com.jingwei.system.domain.model.SysUserWarehouse;
import com.jingwei.system.infrastructure.persistence.SysRoleMapper;
import com.jingwei.system.infrastructure.persistence.SysUserWarehouseMapper;
import com.jingwei.system.infrastructure.persistence.SysUserRoleMapper;
import com.jingwei.system.domain.model.SysUserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限辅助类
 * <p>
 * 为 MyBatis 拦截器提供 Spring Bean 访问能力。
 * 拦截器不是 Spring Bean，无法直接注入，通过此类的静态方法间接访问。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
public class DataPermissionHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private static SysRoleMapper sysRoleMapper;
    private static SysUserRoleMapper sysUserRoleMapper;
    private static SysUserWarehouseMapper sysUserWarehouseMapper;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    /**
     * 获取用户按仓库过滤的允许仓库ID列表
     *
     * @param userId 用户ID
     * @return 允许的仓库ID列表，null 表示全部可见（不过滤），空列表表示无权访问任何仓库
     */
    public static List<Long> getAllowedWarehouseIds(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 1. 查询用户的角色
            SysUserRoleMapper roleMapper = getUserRoleMapper();
            List<SysUserRole> userRoles = roleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, userId));
            if (userRoles.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 查询角色的 data_scope
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            List<SysRole> roles = getSysRoleMapper().selectBatchIds(roleIds);

            // 3. 如果任一角色是 ALL 范围，不过滤
            boolean hasAllScope = roles.stream()
                    .anyMatch(r -> "ALL".equals(r.getDataScope()));
            if (hasAllScope) {
                return null; // null = 不过滤
            }

            // 4. WAREHOUSE 范围：查询用户绑定的仓库ID列表
            SysUserWarehouseMapper uwMapper = getUserWarehouseMapper();
            List<SysUserWarehouse> userWarehouses = uwMapper.selectList(
                    new LambdaQueryWrapper<SysUserWarehouse>()
                            .eq(SysUserWarehouse::getUserId, userId));
            return userWarehouses.stream()
                    .map(SysUserWarehouse::getWarehouseId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询数据权限失败, userId={}: {}", userId, e.getMessage(), e);
            return null; // 出错时不过滤，避免影响正常业务
        }
    }

    private static SysRoleMapper getSysRoleMapper() {
        if (sysRoleMapper == null) {
            sysRoleMapper = applicationContext.getBean(SysRoleMapper.class);
        }
        return sysRoleMapper;
    }

    private static SysUserRoleMapper getUserRoleMapper() {
        if (sysUserRoleMapper == null) {
            sysUserRoleMapper = applicationContext.getBean(SysUserRoleMapper.class);
        }
        return sysUserRoleMapper;
    }

    private static SysUserWarehouseMapper getUserWarehouseMapper() {
        if (sysUserWarehouseMapper == null) {
            sysUserWarehouseMapper = applicationContext.getBean(SysUserWarehouseMapper.class);
        }
        return sysUserWarehouseMapper;
    }
}
