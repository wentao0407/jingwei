package com.jingwei.order.domain.repository;

import com.jingwei.order.domain.model.SalesOrderLine;

import java.util.List;

/**
 * 销售订单行仓库接口
 * <p>
 * 提供销售订单行的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface SalesOrderLineRepository {

    /**
     * 根据订单ID查询所有订单行
     *
     * @param orderId 订单ID
     * @return 订单行列表
     */
    List<SalesOrderLine> selectByOrderId(Long orderId);

    /**
     * 批量插入订单行
     *
     * @param lines 订单行列表
     * @return 插入行数
     */
    int batchInsert(List<SalesOrderLine> lines);

    /**
     * 根据订单ID删除所有订单行（物理删除，用于草稿编辑时替换行数据）
     *
     * @param orderId 订单ID
     * @return 删除行数
     */
    int deleteByOrderId(Long orderId);

    /**
     * 检查同一订单内是否已存在相同的款式+颜色组合
     *
     * @param orderId     订单ID
     * @param spuId       款式ID
     * @param colorWayId  颜色款ID
     * @param excludeLineId 排除的行ID（更新时排除自身，可为 null）
     * @return true=已存在
     */
    boolean existsBySpuAndColor(Long orderId, Long spuId, Long colorWayId, Long excludeLineId);
}
