package com.jingwei.order.domain.repository;

import com.jingwei.order.domain.model.OrderChangeLog;

import java.util.List;

/**
 * 订单变更日志仓库接口
 *
 * @author JingWei
 */
public interface OrderChangeLogRepository {

    /**
     * 插入变更日志
     *
     * @param log 变更日志实体
     * @return 影响行数
     */
    int insert(OrderChangeLog log);

    /**
     * 按订单查询变更日志（按时间倒序）
     *
     * @param orderType 订单类型
     * @param orderId   订单ID
     * @return 变更日志列表
     */
    List<OrderChangeLog> selectByOrder(String orderType, Long orderId);
}
