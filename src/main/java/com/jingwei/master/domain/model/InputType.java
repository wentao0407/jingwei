package com.jingwei.master.domain.model;

/**
 * 属性输入类型枚举
 * <p>
 * 决定前端动态表单的渲染方式：
 * <ul>
 *   <li>TEXT — 文本输入框</li>
 *   <li>NUMBER — 数字输入框</li>
 *   <li>SELECT — 下拉单选</li>
 *   <li>MULTI_SELECT — 下拉多选</li>
 *   <li>COMPOSITION — 成分组分（纤维+百分比的多行输入，合计必须100%）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
public enum InputType {

    /** 文本 */
    TEXT,

    /** 数字 */
    NUMBER,

    /** 下拉单选 */
    SELECT,

    /** 下拉多选 */
    MULTI_SELECT,

    /** 成分组分（纤维+百分比，合计必须100%） */
    COMPOSITION
}
