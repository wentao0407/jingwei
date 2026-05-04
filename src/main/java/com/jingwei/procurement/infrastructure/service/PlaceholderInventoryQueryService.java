package com.jingwei.procurement.infrastructure.service;

import com.jingwei.procurement.domain.service.InventoryQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 库存查询服务占位实现
 * <p>
 * 库存模块（T-29/T-30）实现后替换为真实调用。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
public class PlaceholderInventoryQueryService implements InventoryQueryService {

    @Override
    public BigDecimal getAvailableStock(Long materialId) {
        log.debug("[预留] 查询可用库存: materialId={}, 返回0", materialId);
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getInTransitQuantity(Long materialId) {
        log.debug("[预留] 查询在途数量: materialId={}, 返回0", materialId);
        return BigDecimal.ZERO;
    }
}
