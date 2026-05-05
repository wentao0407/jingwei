package com.jingwei.warehouse.domain.repository;

import com.jingwei.warehouse.domain.model.ReceivingOrder;

/**
 * 收货单仓库接口
 *
 * @author JingWei
 */
public interface ReceivingOrderRepository {

    ReceivingOrder selectById(Long id);

    ReceivingOrder selectDetailById(Long id);

    int insert(ReceivingOrder order);

    int updateById(ReceivingOrder order);
}
