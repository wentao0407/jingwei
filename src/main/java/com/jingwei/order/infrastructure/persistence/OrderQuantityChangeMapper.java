package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.OrderQuantityChange;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数量变更单 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface OrderQuantityChangeMapper extends BaseMapper<OrderQuantityChange> {
}
