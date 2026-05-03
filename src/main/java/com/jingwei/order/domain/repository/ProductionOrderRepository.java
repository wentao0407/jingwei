package com.jingwei.order.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.order.domain.model.ProductionOrder;

/**
 * 生产订单仓库接口
 *
 * @author JingWei
 */
public interface ProductionOrderRepository {

    ProductionOrder selectById(Long id);

    ProductionOrder selectDetailById(Long id);

    IPage<ProductionOrder> selectPage(IPage<ProductionOrder> page,
                                       String status, String orderNo,
                                       String planDateStart, String planDateEnd);

    boolean existsByOrderNo(String orderNo);

    int insert(ProductionOrder order);

    int updateById(ProductionOrder order);

    int deleteById(Long id);
}
