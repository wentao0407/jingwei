package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.InboundOrderLine;

import java.util.List;

/**
 * 入库单行仓库接口
 *
 * @author JingWei
 */
public interface InboundOrderLineRepository {

    List<InboundOrderLine> selectByInboundId(Long inboundId);

    int batchInsert(List<InboundOrderLine> lines);

    int deleteByInboundId(Long inboundId);
}
