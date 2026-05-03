package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生产订单 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ProductionOrderMapper extends BaseMapper<ProductionOrder> {
}
