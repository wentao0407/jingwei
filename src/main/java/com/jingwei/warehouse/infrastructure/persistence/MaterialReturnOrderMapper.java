package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.warehouse.domain.model.MaterialReturnOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaterialReturnOrderMapper extends BaseMapper<MaterialReturnOrder> {
}
