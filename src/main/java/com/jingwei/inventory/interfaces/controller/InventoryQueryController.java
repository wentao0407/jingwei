package com.jingwei.inventory.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.domain.model.R;
import com.jingwei.inventory.application.dto.InventoryStockQueryDTO;
import com.jingwei.inventory.application.service.InventoryQueryApplicationService;
import com.jingwei.inventory.interfaces.vo.InventoryMaterialVO;
import com.jingwei.inventory.interfaces.vo.InventorySkuVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存直接查询 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class InventoryQueryController {

    private final InventoryQueryApplicationService inventoryQueryApplicationService;

    @PostMapping("/inventory/sku/page")
    public R<IPage<InventorySkuVO>> pageSkus(@Valid @RequestBody InventoryStockQueryDTO dto) {
        return R.ok(inventoryQueryApplicationService.pageSkus(dto));
    }

    @PostMapping("/inventory/material/page")
    public R<IPage<InventoryMaterialVO>> pageMaterials(@Valid @RequestBody InventoryStockQueryDTO dto) {
        return R.ok(inventoryQueryApplicationService.pageMaterials(dto));
    }
}
