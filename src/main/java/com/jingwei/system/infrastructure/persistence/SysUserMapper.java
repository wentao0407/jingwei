package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.system.domain.model.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
