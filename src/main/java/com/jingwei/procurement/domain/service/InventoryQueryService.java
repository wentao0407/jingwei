package com.jingwei.procurement.domain.service;

import java.math.BigDecimal;

/**
 * 库存查询服务接口
 * <p>
 * 预留接口，用于 MRP 计算时查询可用库存和在途数量。
 * 库存模块（T-29/T-30）实现后替换为真实调用。
 * </p>
 *
 * @author JingWei
 */
public interface InventoryQueryService {

    /**
     * 查询物料的可用库存
     *
     * @param materialId 物料ID
     * @return 可用库存数量
     */
    BigDecimal getAvailableStock(Long materialId);

    /**
     * 查询物料的在途数量
     *
     * @param materialId 物料ID
     * @return 在途数量
     */
    BigDecimal getInTransitQuantity(Long materialId);
}
