package com.jingwei.order.domain.repository;

import com.jingwei.order.domain.model.ProductionOrderLine;

import java.util.List;

/**
 * 生产订单行仓库接口
 *
 * @author JingWei
 */
public interface ProductionOrderLineRepository {

    List<ProductionOrderLine> selectByOrderId(Long orderId);

    int batchInsert(List<ProductionOrderLine> lines);

    int deleteByOrderId(Long orderId);

    ProductionOrderLine selectById(Long id);

    int updateById(ProductionOrderLine line);

    boolean existsByOrderId(Long orderId);
}
