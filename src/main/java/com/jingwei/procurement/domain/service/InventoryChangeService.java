package com.jingwei.procurement.domain.service;

import java.math.BigDecimal;

/**
 * 库存变更服务接口
 * <p>
 * 预留接口，用于到货检验场景下的库存变更。
 * 库存模块（T-29/T-30）实现后替换为真实调用。
 * </p>
 * <p>
 * 库存流转逻辑：
 * <ul>
 *   <li>收货 → 在途库存减少，质检库存增加</li>
 *   <li>检验合格 → 质检库存减少，可用库存增加</li>
 *   <li>检验不合格 → 质检库存减少</li>
 *   <li>让步接收 → 质检库存减少，可用库存增加（标记降级）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
public interface InventoryChangeService {

    /**
     * 在途库存减少 + 质检库存增加（收货时调用）
     *
     * @param materialId 物料ID
     * @param quantity   数量
     */
    void inTransitToQc(Long materialId, BigDecimal quantity);

    /**
     * 质检库存减少 + 可用库存增加（检验合格时调用）
     *
     * @param materialId 物料ID
     * @param quantity   数量
     */
    void qcToAvailable(Long materialId, BigDecimal quantity);

    /**
     * 质检库存减少（检验不合格退货时调用）
     *
     * @param materialId 物料ID
     * @param quantity   数量
     */
    void qcOut(Long materialId, BigDecimal quantity);
}
