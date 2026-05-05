package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 盘点模式枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum CountMode {

    /** 明盘（可看到系统数量） */
    OPEN("OPEN", "明盘"),
    /** 盲盘（不可看到系统数量，推荐） */
    BLIND("BLIND", "盲盘");

    private final String code;
    private final String label;
}
