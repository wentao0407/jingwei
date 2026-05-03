package com.jingwei.order.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.order.domain.model.SalesOrder;

import java.util.List;

/**
 * 销售订单仓库接口
 * <p>
 * 提供销售订单及订单行的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface SalesOrderRepository {

    /**
     * 根据ID查询销售订单（不含行）
     *
     * @param id 订单ID
     * @return 销售订单实体，不存在返回 null
     */
    SalesOrder selectById(Long id);

    /**
     * 根据ID查询销售订单（含订单行）
     *
     * @param id 订单ID
     * @return 销售订单实体（含 lines），不存在返回 null
     */
    SalesOrder selectDetailById(Long id);

    /**
     * 分页查询销售订单
     *
     * @param page         分页参数
     * @param status       状态筛选（可选）
     * @param customerId   客户ID筛选（可选）
     * @param seasonId     季节ID筛选（可选）
     * @param orderNo      订单编号搜索（模糊，可选）
     * @param orderDateStart 订单日期起始（可选）
     * @param orderDateEnd   订单日期结束（可选）
     * @return 分页结果
     */
    IPage<SalesOrder> selectPage(IPage<SalesOrder> page,
                                  String status, Long customerId, Long seasonId,
                                  String orderNo,
                                  String orderDateStart, String orderDateEnd);

    /**
     * 检查订单编号是否已存在
     *
     * @param orderNo 订单编号
     * @return true=已存在
     */
    boolean existsByOrderNo(String orderNo);

    /**
     * 插入销售订单
     *
     * @param order 销售订单实体
     * @return 影响行数
     */
    int insert(SalesOrder order);

    /**
     * 更新销售订单（乐观锁）
     *
     * @param order 销售订单实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(SalesOrder order);

    /**
     * 逻辑删除销售订单
     *
     * @param id 订单ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
