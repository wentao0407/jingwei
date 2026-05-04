package com.jingwei.procurement.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 尺码用量表值对象
 * <p>
 * 用于 SIZE_DEPENDENT 消耗类型的 BOM 行，存储每个尺码的绝对用量（米/件）。
 * 数据库中以 JSONB 格式存储，结构如下：
 * <pre>
 * {
 *   "baseSizeId": 11,
 *   "baseSizeCode": "M",
 *   "baseConsumption": 1.80,
 *   "sizes": [
 *     {"sizeId": 10, "code": "S",   "consumption": 1.60},
 *     {"sizeId": 11, "code": "M",   "consumption": 1.80},
 *     {"sizeId": 12, "code": "L",   "consumption": 1.95}
 *   ]
 * }
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@Getter
public class SizeConsumptions {

    /** 基准尺码ID */
    private final Long baseSizeId;

    /** 基准尺码编码 */
    private final String baseSizeCode;

    /** 基准用量 */
    private final BigDecimal baseConsumption;

    /** 各尺码用量列表 */
    private final List<SizeConsumptionEntry> sizes;

    @JsonCreator
    public SizeConsumptions(@JsonProperty("baseSizeId") Long baseSizeId,
                            @JsonProperty("baseSizeCode") String baseSizeCode,
                            @JsonProperty("baseConsumption") BigDecimal baseConsumption,
                            @JsonProperty("sizes") List<SizeConsumptionEntry> sizes) {
        this.baseSizeId = baseSizeId;
        this.baseSizeCode = baseSizeCode;
        this.baseConsumption = baseConsumption;
        this.sizes = sizes != null ? Collections.unmodifiableList(new ArrayList<>(sizes))
                : Collections.emptyList();
    }

    /**
     * 根据尺码ID获取用量
     *
     * @param sizeId 尺码ID
     * @return 对应尺码的用量，未找到返回null
     */
    public BigDecimal getConsumption(Long sizeId) {
        return sizes.stream()
                .filter(s -> Objects.equals(s.getSizeId(), sizeId))
                .findFirst()
                .map(SizeConsumptionEntry::getConsumption)
                .orElse(null);
    }

    /**
     * 尺码用量条目
     */
    @Getter
    public static class SizeConsumptionEntry {

        /** 尺码ID */
        private final Long sizeId;

        /** 尺码编码 */
        private final String code;

        /** 绝对用量（米/件） */
        private final BigDecimal consumption;

        @JsonCreator
        public SizeConsumptionEntry(@JsonProperty("sizeId") Long sizeId,
                                    @JsonProperty("code") String code,
                                    @JsonProperty("consumption") BigDecimal consumption) {
            this.sizeId = sizeId;
            this.code = code;
            this.consumption = consumption;
        }
    }
}
