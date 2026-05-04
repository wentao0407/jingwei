package com.jingwei.procurement.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.ProcurementOrder;
import com.jingwei.procurement.domain.model.ProcurementOrderStatus;

/**
 * 采购订单仓库接口
 *
 * @author JingWei
 */
public interface ProcurementOrderRepository {

    ProcurementOrder selectById(Long id);

    ProcurementOrder selectDetailById(Long id);

    IPage<ProcurementOrder> selectPage(IPage<ProcurementOrder> page,
                                        Long supplierId, ProcurementOrderStatus status);

    int insert(ProcurementOrder order);

    int updateById(ProcurementOrder order);
}
