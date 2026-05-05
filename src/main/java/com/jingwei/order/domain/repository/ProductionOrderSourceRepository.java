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

    /**
     * 检查指定销售订单是否已关联生产订单
     *
     * @param salesOrderId 销售订单ID
     * @return true 表示已关联
     */
    boolean existsBySalesOrderId(Long salesOrderId);

    /**
     * 查询指定销售订单行的已转化记录（用于判断是否已全额转化）
     *
     * @param salesLineId 销售订单行ID
     * @return 关联记录列表
     */
    List<ProductionOrderSource> selectBySalesLineId(Long salesLineId);
}
