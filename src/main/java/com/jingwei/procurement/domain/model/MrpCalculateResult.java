package com.jingwei.procurement.domain.model;

import lombok.Getter;

import java.util.List;

/**
 * MRP 计算结果包装
 * <p>
 * 包含计算结果列表和计算过程中的警告信息。
 * 警告用于告知用户哪些需求被跳过（如无BOM、无数量等）。
 * </p>
 *
 * @author JingWei
 */
@Getter
public class MrpCalculateResult {

    private final List<MrpResult> results;
    private final List<String> warnings;

    public MrpCalculateResult(List<MrpResult> results, List<String> warnings) {
        this.results = results;
        this.warnings = warnings;
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}
