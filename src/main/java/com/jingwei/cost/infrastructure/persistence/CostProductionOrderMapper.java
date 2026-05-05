package com.jingwei.cost.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.cost.domain.model.CostProductionOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生产订单成本归集 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface CostProductionOrderMapper extends BaseMapper<CostProductionOrder> {
}
