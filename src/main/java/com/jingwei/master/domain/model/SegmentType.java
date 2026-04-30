package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 编码规则段类型枚举
 * <p>
 * 每个段对应编码中的一个组成部分：
 * <ul>
 *   <li>FIXED — 固定文本，如 SO、RK</li>
 *   <li>DATE — 日期格式，如 YYYYMM、YYYYMMDD</li>
 *   <li>SEQUENCE — 流水号，按配置位数补零</li>
 *   <li>SEASON — 季节编码，从上下文中获取</li>
 *   <li>WAREHOUSE — 仓库编码，从上下文中获取</li>
 *   <li>CUSTOM — 自定义值，从上下文中按 segmentValue 作为 key 获取</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum SegmentType {

    /** 固定文本 */
    FIXED("固定文本"),
    /** 日期格式 */
    DATE("日期格式"),
    /** 流水号 */
    SEQUENCE("流水号"),
    /** 季节编码 */
    SEASON("季节编码"),
    /** 仓库编码 */
    WAREHOUSE("仓库编码"),
    /** 自定义值 */
    CUSTOM("自定义值");

    private final String description;

    SegmentType(String description) {
        this.description = description;
    }
}
