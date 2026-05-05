package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.inventory.domain.model.InventoryAllocation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryAllocationMapper extends BaseMapper<InventoryAllocation> {
}
