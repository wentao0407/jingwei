package com.jingwei.inventory.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.InboundOrder;
import com.jingwei.inventory.domain.model.InboundStatus;

/**
 * 入库单仓库接口
 *
 * @author JingWei
 */
public interface InboundOrderRepository {

    InboundOrder selectById(Long id);

    InboundOrder selectDetailById(Long id);

    IPage<InboundOrder> selectPage(Page<InboundOrder> page, InboundStatus status, Long warehouseId, String inboundNo);

    int insert(InboundOrder order);

    int updateById(InboundOrder order);

    int deleteById(Long id);
}
