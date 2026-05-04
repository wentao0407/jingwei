package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * 采购订单状态机事件枚举
 *
 * @author JingWei
 */
@Getter
public enum ProcurementOrderEvent {

    SUBMIT("提交审批"),
    APPROVE("审批通过"),
    REJECT("审批驳回"),
    RESUBMIT("重新提交"),
    ISSUE("下发供应商"),
    RECEIVE("收货"),
    COMPLETE("完成");

    private final String label;

    ProcurementOrderEvent(String label) {
        this.label = label;
    }
}
