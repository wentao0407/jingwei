package com.jingwei.order.domain.repository;

import com.jingwei.order.domain.model.ReturnOrderLine;

import java.util.List;

/**
 * 退货单行仓储接口
 *
 * @author JingWei
 */
public interface ReturnOrderLineRepository {

    /**
     * 批量插入退货行
     *
     * @param lines 退货行列表
     */
    void insertBatch(List<ReturnOrderLine> lines);

    /**
     * 根据退货单ID查询所有行
     *
     * @param returnId 退货单ID
     * @return 退货行列表
     */
    List<ReturnOrderLine> selectByReturnId(Long returnId);

    /**
     * 更新退货行
     *
     * @param line 退货行
     * @return 影响行数
     */
    int updateById(ReturnOrderLine line);

    /**
     * 根据原销售订单行ID查询退货数量合计
     * <p>
     * 用于累计校验：同一订单行的退货总量不能超过已发货数量
     * </p>
     *
     * @param salesOrderLineId 原销售订单行ID
     * @return 退货数量合计
     */
    int sumReturnQtyBySalesOrderLineId(Long salesOrderLineId);
}
