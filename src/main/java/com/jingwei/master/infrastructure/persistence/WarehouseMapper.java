package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Warehouse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
}
