package com.jingwei.procurement.domain.repository;

import com.jingwei.procurement.domain.model.ProcurementOrderLine;

import java.util.List;

/**
 * 采购订单行仓库接口
 *
 * @author JingWei
 */
public interface ProcurementOrderLineRepository {

    ProcurementOrderLine selectById(Long id);

    List<ProcurementOrderLine> selectByOrderId(Long orderId);

    int insert(ProcurementOrderLine line);

    int updateById(ProcurementOrderLine line);

    int deleteByOrderId(Long orderId);
}
