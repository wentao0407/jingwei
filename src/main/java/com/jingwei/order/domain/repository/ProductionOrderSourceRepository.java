package com.jingwei.order.domain.repository;

import com.jingwei.order.domain.model.ProductionOrderSource;

import java.util.List;

/**
 * 生产订单与销售订单关联仓库接口
 *
 * @author JingWei
 */
public interface ProductionOrderSourceRepository {

    List<ProductionOrderSource> selectByProductionOrderId(Long productionOrderId);

    List<ProductionOrderSource> selectBySalesOrderId(Long salesOrderId);

    int batchInsert(List<ProductionOrderSource> sources);

    int deleteByProductionOrderId(Long productionOrderId);
}
