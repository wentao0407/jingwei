package com.jingwei.inventory.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.application.dto.InventoryStockQueryDTO;
import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.model.InventorySku;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.interfaces.vo.InventoryMaterialVO;
import com.jingwei.inventory.interfaces.vo.InventorySkuVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryQueryApplicationServiceTest {

    @Test
    @DisplayName("库存SKU分页查询应下推过滤条件到仓储层")
    void pageSkus_shouldUseRepositoryPagination() {
        FakeInventorySkuRepository skuRepository = new FakeInventorySkuRepository();
        InventorySku record = new InventorySku();
        record.setId(1L);
        record.setSkuId(90001L);
        record.setWarehouseId(30001L);
        record.setBatchNo("B-01");
        record.setAvailableQty(8);
        skuRepository.records.add(record);
        InventoryQueryApplicationService service = new InventoryQueryApplicationService(
                skuRepository,
                new FakeInventoryMaterialRepository());
        InventoryStockQueryDTO query = new InventoryStockQueryDTO();
        query.setCurrent(1L);
        query.setSize(20L);
        query.setSkuId(90001L);
        query.setWarehouseId(30001L);
        query.setBatchNo(" B-01 ");

        Page<InventorySkuVO> result = service.pageSkus(query);

        assertEquals(1, result.getTotal());
        assertEquals(90001L, result.getRecords().get(0).getSkuId());
        assertEquals(90001L, skuRepository.lastSkuId);
        assertEquals(30001L, skuRepository.lastWarehouseId);
        assertEquals(" B-01 ", skuRepository.lastBatchNo);
    }

    @Test
    @DisplayName("库存物料分页查询应下推过滤条件到仓储层")
    void pageMaterials_shouldUseRepositoryPagination() {
        FakeInventoryMaterialRepository materialRepository = new FakeInventoryMaterialRepository();
        InventoryMaterial record = new InventoryMaterial();
        record.setId(2L);
        record.setMaterialId(80001L);
        record.setWarehouseId(30002L);
        record.setBatchNo("M-01");
        record.setAvailableQty(new BigDecimal("12.5"));
        materialRepository.records.add(record);
        InventoryQueryApplicationService service = new InventoryQueryApplicationService(
                new FakeInventorySkuRepository(),
                materialRepository);
        InventoryStockQueryDTO query = new InventoryStockQueryDTO();
        query.setCurrent(1L);
        query.setSize(20L);
        query.setMaterialId(80001L);
        query.setWarehouseId(30002L);
        query.setBatchNo(" M-01 ");

        Page<InventoryMaterialVO> result = service.pageMaterials(query);

        assertEquals(1, result.getTotal());
        assertEquals(80001L, result.getRecords().get(0).getMaterialId());
        assertEquals(80001L, materialRepository.lastMaterialId);
        assertEquals(30002L, materialRepository.lastWarehouseId);
        assertEquals(" M-01 ", materialRepository.lastBatchNo);
    }

    private static class FakeInventorySkuRepository implements InventorySkuRepository {
        private final List<InventorySku> records = new ArrayList<>();
        private Long lastSkuId;
        private Long lastWarehouseId;
        private String lastBatchNo;

        @Override
        public InventorySku selectById(Long id) {
            return null;
        }

        @Override
        public InventorySku selectBySkuAndWarehouseAndBatch(Long skuId, Long warehouseId, String batchNo) {
            return null;
        }

        @Override
        public List<InventorySku> selectBySkuId(Long skuId) {
            return List.of();
        }

        @Override
        public List<InventorySku> selectBySkuAndWarehouse(Long skuId, Long warehouseId) {
            return List.of();
        }

        @Override
        public List<InventorySku> selectAll() {
            return records;
        }

        @Override
        public Page<InventorySku> pageQuery(Long current, Long size, Long skuId, Long warehouseId, String batchNo) {
            lastSkuId = skuId;
            lastWarehouseId = warehouseId;
            lastBatchNo = batchNo;
            Page<InventorySku> page = new Page<>(current, size);
            page.setRecords(records);
            page.setTotal(records.size());
            return page;
        }

        @Override
        public int insert(InventorySku record) {
            return 0;
        }

        @Override
        public int updateById(InventorySku record) {
            return 0;
        }
    }

    private static class FakeInventoryMaterialRepository implements InventoryMaterialRepository {
        private final List<InventoryMaterial> records = new ArrayList<>();
        private Long lastMaterialId;
        private Long lastWarehouseId;
        private String lastBatchNo;

        @Override
        public InventoryMaterial selectById(Long id) {
            return null;
        }

        @Override
        public InventoryMaterial selectByMaterialAndWarehouseAndBatch(Long materialId, Long warehouseId, String batchNo) {
            return null;
        }

        @Override
        public List<InventoryMaterial> selectByMaterialId(Long materialId) {
            return List.of();
        }

        @Override
        public List<InventoryMaterial> selectByMaterialAndWarehouse(Long materialId, Long warehouseId) {
            return List.of();
        }

        @Override
        public List<InventoryMaterial> selectAll() {
            return records;
        }

        @Override
        public Page<InventoryMaterial> pageQuery(Long current, Long size, Long materialId, Long warehouseId, String batchNo) {
            lastMaterialId = materialId;
            lastWarehouseId = warehouseId;
            lastBatchNo = batchNo;
            Page<InventoryMaterial> page = new Page<>(current, size);
            page.setRecords(records);
            page.setTotal(records.size());
            return page;
        }

        @Override
        public int insert(InventoryMaterial record) {
            return 0;
        }

        @Override
        public int updateById(InventoryMaterial record) {
            return 0;
        }
    }
}
