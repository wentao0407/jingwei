package com.jingwei.procurement.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验结果值对象
 * <p>
 * 存储为 JSONB，包含检验人、检验时间、各检验项结果和综合结论。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class QcResult {

    /** 检验人姓名 */
    private String inspector;

    /** 检验时间 */
    private LocalDateTime inspectedAt;

    /** 检验项列表 */
    private List<QcItem> items;

    /** 综合结果 */
    private String overallResult;

    /** 结论说明 */
    private String conclusion;

    /**
     * 单个检验项
     */
    @Getter
    @Setter
    public static class QcItem {

        /** 检验项名称（如：色差、克重、门幅） */
        private String name;

        /** 标准要求 */
        private String standard;

        /** 实际值 */
        private String actual;

        /** 检验结果：PASS/FAIL */
        private String result;
    }
}
