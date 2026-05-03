package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.ProductionOrderLine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生产订单行 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ProductionOrderLineMapper extends BaseMapper<ProductionOrderLine> {
}
