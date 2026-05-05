package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.inventory.domain.model.InventoryMaterial;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryMaterialMapper extends BaseMapper<InventoryMaterial> {
}
