package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.system.domain.model.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
