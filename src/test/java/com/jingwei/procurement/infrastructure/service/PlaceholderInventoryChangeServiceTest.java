package com.jingwei.procurement.infrastructure.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.service.DomainEventPublisher;
import com.jingwei.inventory.domain.model.InTransitStatus;
import com.jingwei.inventory.domain.model.InventoryInTransit;
import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.model.OperationType;
import com.jingwei.inventory.domain.repository.InventoryInTransitRepository;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventoryOperationRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.service.ChangeInventoryCommand;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.procurement.domain.service.InventoryChangeContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaceholderInventoryChangeServiceTest {

    @Test
    @DisplayName("收货入质检应按采购行、仓库和批次定位库存记录")
    void inTransitToQc_shouldUseProcurementLineWarehouseAndBatch() {
        FakeInventoryDomainService inventoryDomainService = new FakeInventoryDomainService();
        FakeInventoryMaterialRepository materialRepository = new FakeInventoryMaterialRepository();
        FakeInventoryInTransitRepository inTransitRepository = new FakeInventoryInTransitRepository();
        inTransitRepository.records.add(buildInTransit(30001L, 60001L, 90001L, 10001L, new BigDecimal("80")));
        materialRepository.records.add(buildMaterial(70001L, 90001L, 20001L, "B20260501"));
        materialRepository.records.add(buildMaterial(70002L, 90001L, 10001L, "B20260501"));
        PlaceholderInventoryChangeService service = new PlaceholderInventoryChangeService(
                inventoryDomainService, materialRepository, inTransitRepository);

        service.inTransitToQc(new InventoryChangeContext(
                90001L, 60001L, null, "B20260501", new BigDecimal("12")));

        ChangeInventoryCommand command = inventoryDomainService.lastCommand;
        assertEquals(OperationType.INBOUND_PURCHASE, command.getOperationType());
        assertEquals(70002L, command.getInventoryId());
        assertEquals(10001L, command.getWarehouseId());
        assertEquals("B20260501", command.getBatchNo());
        assertEquals(new BigDecimal("12"), command.getQuantity());
        assertEquals(new BigDecimal("12"), inTransitRepository.records.get(0).getReceivedQty());
        assertEquals(new BigDecimal("68"), inTransitRepository.records.get(0).getRemainingQty());
    }

    @Test
    @DisplayName("缺少仓库上下文时不能静默跳过库存变更")
    void inTransitToQc_missingWarehouse_shouldThrow() {
        PlaceholderInventoryChangeService service = new PlaceholderInventoryChangeService(
                new FakeInventoryDomainService(),
                new FakeInventoryMaterialRepository(),
                new FakeInventoryInTransitRepository());

        assertThrows(BizException.class, () -> service.inTransitToQc(new InventoryChangeContext(
                90001L, 60001L, null, "B20260501", new BigDecimal("12"))));
    }

    @Test
    @DisplayName("已全部收货的采购行仍可为质检提供仓库上下文")
    void qcToAvailable_shouldUseFullyReceivedProcurementLineWarehouse() {
        FakeInventoryDomainService inventoryDomainService = new FakeInventoryDomainService();
        FakeInventoryMaterialRepository materialRepository = new FakeInventoryMaterialRepository();
        FakeInventoryInTransitRepository inTransitRepository = new FakeInventoryInTransitRepository();
        InventoryInTransit inTransit = buildInTransit(30001L, 60001L, 90001L, 10001L, BigDecimal.ZERO);
        inTransit.setStatus(InTransitStatus.FULLY_RECEIVED);
        inTransitRepository.records.add(inTransit);
        materialRepository.records.add(buildMaterial(70001L, 90001L, 10001L, "B20260501"));
        PlaceholderInventoryChangeService service = new PlaceholderInventoryChangeService(
                inventoryDomainService, materialRepository, inTransitRepository);

        service.qcToAvailable(new InventoryChangeContext(
                90001L, 60001L, null, "B20260501", new BigDecimal("12")));

        ChangeInventoryCommand command = inventoryDomainService.lastCommand;
        assertEquals(OperationType.QC_PASS, command.getOperationType());
        assertEquals(70001L, command.getInventoryId());
        assertEquals(10001L, command.getWarehouseId());
        assertEquals("B20260501", command.getBatchNo());
        assertEquals(new BigDecimal("12"), command.getQuantity());
    }

    private InventoryInTransit buildInTransit(
            Long id,
            Long procurementLineId,
            Long materialId,
            Long warehouseId,
            BigDecimal remainingQty) {
        InventoryInTransit record = new InventoryInTransit();
        record.setId(id);
        record.setProcurementLineId(procurementLineId);
        record.setMaterialId(materialId);
        record.setWarehouseId(warehouseId);
        record.setRemainingQty(remainingQty);
        record.setReceivedQty(BigDecimal.ZERO);
        record.setStatus(InTransitStatus.PENDING);
        return record;
    }

    private InventoryMaterial buildMaterial(Long id, Long materialId, Long warehouseId, String batchNo) {
        InventoryMaterial record = new InventoryMaterial();
        record.setId(id);
        record.setMaterialId(materialId);
        record.setWarehouseId(warehouseId);
        record.setBatchNo(batchNo);
        record.setAvailableQty(BigDecimal.ZERO);
        record.setLockedQty(BigDecimal.ZERO);
        record.setQcQty(BigDecimal.ZERO);
        record.setTotalQty(BigDecimal.ZERO);
        record.setInTransitQty(BigDecimal.ZERO);
        return record;
    }

    private static class FakeInventoryDomainService extends InventoryDomainService {
        private ChangeInventoryCommand lastCommand;

        FakeInventoryDomainService() {
            super(null, null, null, null, null);
        }

        @Override
        public void changeInventory(ChangeInventoryCommand cmd) {
            this.lastCommand = cmd;
        }
    }

    private static class FakeInventoryMaterialRepository implements InventoryMaterialRepository {
        private final List<InventoryMaterial> records = new ArrayList<>();

        @Override
        public InventoryMaterial selectById(Long id) {
            return records.stream().filter(record -> id.equals(record.getId())).findFirst().orElse(null);
        }

        @Override
        public InventoryMaterial selectByMaterialAndWarehouseAndBatch(Long materialId, Long warehouseId, String batchNo) {
            return records.stream()
                    .filter(record -> materialId.equals(record.getMaterialId()))
                    .filter(record -> warehouseId.equals(record.getWarehouseId()))
                    .filter(record -> batchNo.equals(record.getBatchNo()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<InventoryMaterial> selectByMaterialId(Long materialId) {
            return records.stream().filter(record -> materialId.equals(record.getMaterialId())).toList();
        }

        @Override
        public List<InventoryMaterial> selectByMaterialAndWarehouse(Long materialId, Long warehouseId) {
            return records.stream()
                    .filter(record -> materialId.equals(record.getMaterialId()))
                    .filter(record -> warehouseId.equals(record.getWarehouseId()))
                    .toList();
        }

        @Override
        public List<InventoryMaterial> selectAll() {
            return records;
        }

        @Override
        public Page<InventoryMaterial> pageQuery(Long current, Long size, Long materialId, Long warehouseId, String batchNo) {
            Page<InventoryMaterial> page = new Page<>(current == null ? 1L : current, size == null ? 20L : size);
            page.setRecords(records.stream()
                    .filter(record -> materialId == null || materialId.equals(record.getMaterialId()))
                    .filter(record -> warehouseId == null || warehouseId.equals(record.getWarehouseId()))
                    .filter(record -> batchNo == null || batchNo.isBlank() || batchNo.trim().equals(record.getBatchNo()))
                    .toList());
            page.setTotal(page.getRecords().size());
            return page;
        }

        @Override
        public int insert(InventoryMaterial record) {
            record.setId(900000L + records.size());
            records.add(record);
            return 1;
        }

        @Override
        public int updateById(InventoryMaterial record) {
            return 1;
        }
    }

    private static class FakeInventoryInTransitRepository implements InventoryInTransitRepository {
        private final List<InventoryInTransit> records = new ArrayList<>();

        @Override
        public InventoryInTransit selectById(Long id) {
            return records.stream().filter(record -> id.equals(record.getId())).findFirst().orElse(null);
        }

        @Override
        public List<InventoryInTransit> selectByProcurementOrderId(Long procurementOrderId) {
            return records.stream().filter(record -> procurementOrderId.equals(record.getProcurementOrderId())).toList();
        }

        @Override
        public List<InventoryInTransit> selectByMaterialId(Long materialId) {
            return records.stream().filter(record -> materialId.equals(record.getMaterialId())).toList();
        }

        @Override
        public List<InventoryInTransit> selectByProcurementLineId(Long procurementLineId) {
            return records.stream().filter(record -> procurementLineId.equals(record.getProcurementLineId())).toList();
        }

        @Override
        public int insert(InventoryInTransit record) {
            records.add(record);
            return 1;
        }

        @Override
        public int updateById(InventoryInTransit record) {
            return 1;
        }
    }
}
