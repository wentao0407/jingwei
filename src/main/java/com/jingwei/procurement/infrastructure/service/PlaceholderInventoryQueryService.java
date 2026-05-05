package com.jingwei.procurement.infrastructure.service;

import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.procurement.domain.service.InventoryQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存查询服务真实实现
 * <p>
 * 查询原料库存的可用数量和在途数量，供 MRP 计算使用。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceholderInventoryQueryService implements InventoryQueryService {

    private final InventoryMaterialRepository inventoryMaterialRepository;

    @Override
    public BigDecimal getAvailableStock(Long materialId) {
        List<InventoryMaterial> records = inventoryMaterialRepository.selectByMaterialId(materialId);
        BigDecimal total = records.stream()
                .map(r -> r.getAvailableQty() != null ? r.getAvailableQty() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("查询可用库存: materialId={}, available={}", materialId, total);
        return total;
    }

    @Override
    public BigDecimal getInTransitQuantity(Long materialId) {
        // TODO: InventoryInTransitRepository 需增加 selectByMaterialId 方法后替换
        log.debug("查询在途数量: materialId={}, 暂返回0（在途查询待完善）", materialId);
        return BigDecimal.ZERO;
    }
}
