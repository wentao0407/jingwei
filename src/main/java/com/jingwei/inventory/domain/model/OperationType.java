package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 库存操作类型枚举
 * <p>
 * 每种操作类型对应一组固定的字段变更规则，
 * 由 {@link com.jingwei.inventory.domain.service.InventoryDomainService} 执行。
 * </p>
 * <p>
 * 字段变更规则：
 * <ul>
 *   <li>INBOUND_PURCHASE — qc += N, total += N</li>
 *   <li>INBOUND_PRODUCTION — available += N, total += N</li>
 *   <li>INBOUND_RETURN — qc += N, total += N</li>
 *   <li>QC_PASS — available += N, qc -= N, total 不变</li>
 *   <li>QC_FAIL — qc -= N, total -= N</li>
 *   <li>ALLOCATE — available -= N, locked += N, total 不变</li>
 *   <li>RELEASE — available += N, locked -= N, total 不变</li>
 *   <li>OUTBOUND_SALES — locked -= N, total -= N</li>
 *   <li>OUTBOUND_MATERIAL — available -= N, total -= N</li>
 *   <li>ADJUST_GAIN — available += N, total += N</li>
 *   <li>ADJUST_LOSS — available -= N, total -= N</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum OperationType {

    /** 采购到货入库（→质检库存） */
    INBOUND_PURCHASE("INBOUND_PURCHASE", "采购到货"),

    /** 生产完工入库（→可用库存） */
    INBOUND_PRODUCTION("INBOUND_PRODUCTION", "生产入库"),

    /** 客户退货入库（→质检库存） */
    INBOUND_RETURN("INBOUND_RETURN", "退货入库"),

    /** 质检合格（质检→可用） */
    QC_PASS("QC_PASS", "质检合格"),

    /** 质检不合格（质检扣减，退货出库） */
    QC_FAIL("QC_FAIL", "质检不合格"),

    /** 锁定预留（可用→锁定） */
    ALLOCATE("ALLOCATE", "锁定预留"),

    /** 释放预留（锁定→可用） */
    RELEASE("RELEASE", "释放预留"),

    /** 销售出库（从锁定扣减） */
    OUTBOUND_SALES("OUTBOUND_SALES", "销售出库"),

    /** 领料出库（从可用扣减） */
    OUTBOUND_MATERIAL("OUTBOUND_MATERIAL", "领料出库"),

    /** 盘点盈余/手工调增 */
    ADJUST_GAIN("ADJUST_GAIN", "盘盈调增"),

    /** 盘点亏损/手工调减 */
    ADJUST_LOSS("ADJUST_LOSS", "盘亏调减");

    private final String code;
    private final String label;
}
