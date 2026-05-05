package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.inventory.domain.model.InventoryInTransit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryInTransitMapper extends BaseMapper<InventoryInTransit> {
}
