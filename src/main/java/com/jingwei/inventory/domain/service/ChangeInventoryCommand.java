package com.jingwei.inventory.domain.service;

import com.jingwei.inventory.domain.model.InventoryType;
import com.jingwei.inventory.domain.model.OperationType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 库存变更命令对象
 * <p>
 * 封装一次库存变更的全部参数，作为
 * {@link InventoryDomainService#changeInventory(ChangeInventoryCommand)} 的入参。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class ChangeInventoryCommand {

    /** 操作类型 */
    private OperationType operationType;

    /** 库存类型（SKU 或 MATERIAL） */
    private InventoryType inventoryType;

    /** 库存记录ID（inventory_sku 或 inventory_material 的 ID） */
    private Long inventoryId;

    /** SKU ID（成品时使用） */
    private Long skuId;

    /** 物料ID（原料时使用） */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 操作数量（正数） */
    private BigDecimal quantity;

    /** 来源单据类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据编号 */
    private String sourceNo;

    /** 本次操作的单位成本（入库时使用） */
    private BigDecimal unitCost;

    /** 操作人ID */
    private Long operatorId;

    /** 备注 */
    private String remark;

    /**
     * 创建成品库存变更命令的工厂方法
     */
    public static ChangeInventoryCommand forSku(OperationType opType, Long inventoryId,
                                                  Long skuId, Long warehouseId,
                                                  String batchNo, int quantity,
                                                  String sourceType, Long sourceId,
                                                  Long operatorId) {
        ChangeInventoryCommand cmd = new ChangeInventoryCommand();
        cmd.setOperationType(opType);
        cmd.setInventoryType(InventoryType.SKU);
        cmd.setInventoryId(inventoryId);
        cmd.setSkuId(skuId);
        cmd.setWarehouseId(warehouseId);
        cmd.setBatchNo(batchNo);
        cmd.setQuantity(BigDecimal.valueOf(quantity));
        cmd.setSourceType(sourceType);
        cmd.setSourceId(sourceId);
        cmd.setOperatorId(operatorId);
        return cmd;
    }
}
