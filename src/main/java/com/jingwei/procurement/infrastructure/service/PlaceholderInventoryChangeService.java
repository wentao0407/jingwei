package com.jingwei.procurement.infrastructure.service;

import com.jingwei.procurement.domain.service.InventoryChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 库存变更服务占位实现
 * <p>
 * 库存模块（T-29/T-30）实现后替换为真实调用。
 * 当前仅记录日志，不实际变更库存。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
public class PlaceholderInventoryChangeService implements InventoryChangeService {

    @Override
    public void inTransitToQc(Long materialId, BigDecimal quantity) {
        log.info("[占位] 在途→质检: materialId={}, quantity={}", materialId, quantity);
    }

    @Override
    public void qcToAvailable(Long materialId, BigDecimal quantity) {
        log.info("[占位] 质检→可用: materialId={}, quantity={}", materialId, quantity);
    }

    @Override
    public void qcOut(Long materialId, BigDecimal quantity) {
        log.info("[占位] 质检出库（退货）: materialId={}, quantity={}", materialId, quantity);
    }
}
