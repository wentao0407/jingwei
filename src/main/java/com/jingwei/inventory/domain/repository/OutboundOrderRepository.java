package com.jingwei.inventory.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.OutboundOrder;
import com.jingwei.inventory.domain.model.OutboundStatus;

/**
 * 出库单仓库接口
 *
 * @author JingWei
 */
public interface OutboundOrderRepository {

    OutboundOrder selectById(Long id);

    OutboundOrder selectDetailById(Long id);

    IPage<OutboundOrder> selectPage(Page<OutboundOrder> page, OutboundStatus status, Long warehouseId, String outboundNo);

    int insert(OutboundOrder order);

    int updateById(OutboundOrder order);

    int deleteById(Long id);
}
