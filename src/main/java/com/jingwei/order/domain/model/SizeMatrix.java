package com.jingwei.order.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 尺码矩阵值对象
 * <p>
 * 封装销售订单行和生产订单行中的尺码数量矩阵，负责矩阵的求和、校验和差异计算。
 * 数据库中以 JSONB 格式存储，结构如下：
 * <pre>
 * {
 *   "sizeGroupId": 1,
 *   "sizes": [
 *     {"sizeId": 10, "code": "S",  "quantity": 100},
 *     {"sizeId": 11, "code": "M",  "quantity": 200},
 *     {"sizeId": 12, "code": "L",  "quantity": 300}
 *   ],
 *   "totalQuantity": 600
 * }
 * </pre>
 * </p>
 * <p>
 * 设计原则：
 * <ul>
 *   <li>值对象 — 不可变，修改操作返回新实例</li>
 *   <li>自校验 — 创建时自动校验 totalQuantity 与 sizes 求和一致性</li>
 *   <li>自计算 — totalQuantity 由 sizes 列表求和自动计算，不依赖外部传入</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public class SizeMatrix {

    /** 尺码组ID（关联 t_md_size_group） */
    private final Long sizeGroupId;

    /** 尺码数量列表（按 sort_order 排序） */
    private final List<SizeEntry> sizes;

    /** 总数量（所有尺码数量之和，自动计算） */
    private final int totalQuantity;

    /**
     * 全参构造（自动计算 totalQuantity）
     *
     * @param sizeGroupId 尺码组ID
     * @param sizes       尺码数量列表
     */
    @JsonCreator
    public SizeMatrix(@JsonProperty("sizeGroupId") Long sizeGroupId,
                      @JsonProperty("sizes") List<SizeEntry> sizes) {
        if (sizeGroupId == null) {
            throw new IllegalArgumentException("尺码组ID不能为空");
        }
        if (sizes == null || sizes.isEmpty()) {
            throw new IllegalArgumentException("尺码列表不能为空");
        }
        this.sizeGroupId = sizeGroupId;
        // 防御性拷贝，保证不可变
        this.sizes = Collections.unmodifiableList(new ArrayList<>(sizes));
        // 自动计算总数量
        this.totalQuantity = calculateTotalQuantity();
    }

    /**
     * 计算矩阵总数量
     * <p>
     * 遍历所有尺码的 quantity 求和。
     * </p>
     *
     * @return 总数量
     */
    private int calculateTotalQuantity() {
        return sizes.stream()
                .mapToInt(SizeEntry::getQuantity)
                .sum();
    }

    /**
     * 校验矩阵数据一致性
     * <p>
     * 检查项：
     * <ul>
     *   <li>尺码列表中不允许有重复的 sizeId</li>
     *   <li>不允许负数数量</li>
     * </ul>
     * </p>
     *
     @return 校验通过返回 true
     */
    public boolean validate() {
        // 检查 sizeId 重复
        long distinctCount = sizes.stream()
                .map(SizeEntry::getSizeId)
                .distinct()
                .count();
        if (distinctCount != sizes.size()) {
            return false;
        }

        // 检查负数数量
        return sizes.stream().noneMatch(s -> s.getQuantity() < 0);
    }

    /**
     * 计算与另一个矩阵的差异
     * <p>
     * 用于数量变更单的差异矩阵计算。
     * 差异值 = 当前矩阵数量 - 目标矩阵数量。
     * </p>
     *
     * @param other 另一个矩阵
     * @return 差异矩阵（同结构，quantity 为差值）
     */
    public SizeMatrix diff(SizeMatrix other) {
        if (!this.sizeGroupId.equals(other.sizeGroupId)) {
            throw new IllegalArgumentException("只能对相同尺码组的矩阵计算差异");
        }

        List<SizeEntry> diffSizes = new ArrayList<>();
        for (SizeEntry thisEntry : this.sizes) {
            SizeEntry otherEntry = other.sizes.stream()
                    .filter(s -> s.getSizeId().equals(thisEntry.getSizeId()))
                    .findFirst()
                    .orElse(new SizeEntry(thisEntry.getSizeId(), thisEntry.getCode(), 0));
            diffSizes.add(new SizeEntry(thisEntry.getSizeId(), thisEntry.getCode(),
                    thisEntry.getQuantity() - otherEntry.getQuantity()));
        }

        return new SizeMatrix(this.sizeGroupId, diffSizes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SizeMatrix that = (SizeMatrix) o;
        return totalQuantity == that.totalQuantity
                && Objects.equals(sizeGroupId, that.sizeGroupId)
                && Objects.equals(sizes, that.sizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sizeGroupId, sizes, totalQuantity);
    }

    /**
     * 尺码条目 — 矩阵中的一个尺码及对应数量
     * <p>
     * 不可变值对象，表示 "某个尺码有多少件"。
     * </p>
     *
     * @author JingWei
     */
    @Getter
    public static class SizeEntry {

        /** 尺码ID（关联 t_md_size） */
        private final Long sizeId;

        /** 尺码编码（如 S/M/L/XL，冗余存储方便展示） */
        private final String code;

        /** 数量 */
        private final int quantity;

        /**
         * 全参构造
         *
         * @param sizeId   尺码ID
         * @param code     尺码编码
         * @param quantity 数量
         */
        @JsonCreator
        public SizeEntry(@JsonProperty("sizeId") Long sizeId,
                         @JsonProperty("code") String code,
                         @JsonProperty("quantity") int quantity) {
            if (sizeId == null) {
                throw new IllegalArgumentException("尺码ID不能为空");
            }
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("尺码编码不能为空");
            }
            this.sizeId = sizeId;
            this.code = code;
            this.quantity = quantity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SizeEntry sizeEntry = (SizeEntry) o;
            return quantity == sizeEntry.quantity
                    && Objects.equals(sizeId, sizeEntry.sizeId)
                    && Objects.equals(code, sizeEntry.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sizeId, code, quantity);
        }
    }
}
