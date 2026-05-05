package com.jingwei.order.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.order.domain.model.ReturnOrder;
import com.jingwei.order.domain.model.ReturnStatus;

/**
 * 退货单仓储接口
 *
 * @author JingWei
 */
public interface ReturnOrderRepository {

    /**
     * 插入退货单
     *
     * @param returnOrder 退货单
     */
    void insert(ReturnOrder returnOrder);

    /**
     * 根据ID查询退货单
     *
     * @param id 退货单ID
     * @return 退货单
     */
    ReturnOrder selectById(Long id);

    /**
     * 更新退货单
     *
     * @param returnOrder 退货单
     * @return 影响行数
     */
    int updateById(ReturnOrder returnOrder);

    /**
     * 分页查询退货单
     *
     * @param page       分页参数
     * @param customerId 客户ID（可选）
     * @param status     状态（可选）
     * @return 分页结果
     */
    Page<ReturnOrder> selectPage(Page<ReturnOrder> page, Long customerId, ReturnStatus status);

    /**
     * 根据退货单号查询
     *
     * @param returnNo 退货单号
     * @return 退货单
     */
    ReturnOrder selectByReturnNo(String returnNo);
}
