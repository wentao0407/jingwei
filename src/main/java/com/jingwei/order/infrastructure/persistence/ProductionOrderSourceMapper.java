package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.ProductionOrderSource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生产订单与销售订单关联 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ProductionOrderSourceMapper extends BaseMapper<ProductionOrderSource> {
}
