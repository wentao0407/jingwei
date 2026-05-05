package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.system.domain.model.SysUserWarehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户仓库权限 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SysUserWarehouseMapper extends BaseMapper<SysUserWarehouse> {
}
