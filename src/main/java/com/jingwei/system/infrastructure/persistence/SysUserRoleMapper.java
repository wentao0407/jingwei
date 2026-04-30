package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.system.domain.model.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
