package com.jingwei.order.domain.repository;

import com.jingwei.order.domain.model.OrderQuantityChange;

import java.util.List;

/**
 * 数量变更单仓库接口
 *
 * @author JingWei
 */
public interface OrderQuantityChangeRepository {

    /**
     * 插入变更单
     *
     * @param change 变更单实体
     * @return 影响行数
     */
    int insert(OrderQuantityChange change);

    /**
     * 根据ID查询变更单
     *
     * @param id 变更单ID
     * @return 变更单实体
     */
    OrderQuantityChange selectById(Long id);

    /**
     * 更新变更单
     *
     * @param change 变更单实体
     * @return 影响行数
     */
    int updateById(OrderQuantityChange change);

    /**
     * 按订单ID查询所有变更单
     *
     * @param orderId 订单ID
     * @return 变更单列表
     */
    List<OrderQuantityChange> selectByOrderId(Long orderId);

    /**
     * 按订单行ID查询变更单
     *
     * @param orderLineId 订单行ID
     * @return 变更单列表
     */
    List<OrderQuantityChange> selectByOrderLineId(Long orderLineId);
}
