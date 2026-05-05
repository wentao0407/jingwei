package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.inventory.domain.model.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {
}
