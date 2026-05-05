package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.OutboundOrderLine;

import java.util.List;

/**
 * 出库单行仓库接口
 *
 * @author JingWei
 */
public interface OutboundOrderLineRepository {

    List<OutboundOrderLine> selectByOutboundId(Long outboundId);

    int batchInsert(List<OutboundOrderLine> lines);

    int deleteByOutboundId(Long outboundId);
}
