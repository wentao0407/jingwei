package com.jingwei.order.domain.model;

import com.jingwei.order.domain.model.SizeMatrix.SizeEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SizeMatrix 值对象单元测试
 * <p>
 * 覆盖验收标准中的核心功能：
 * <ul>
 *   <li>矩阵创建和自动求和</li>
 *   <li>矩阵校验（重复 sizeId、负数数量）</li>
 *   <li>差异矩阵计算</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
class SizeMatrixTest {

    private List<SizeEntry> standardSizes() {
        return List.of(
                new SizeEntry(10L, "S", 100),
                new SizeEntry(11L, "M", 200),
                new SizeEntry(12L, "L", 300),
                new SizeEntry(13L, "XL", 200),
                new SizeEntry(14L, "XXL", 100)
        );
    }

    @Nested
    @DisplayName("创建与求和")
    class CreationAndSum {

        @Test
        @DisplayName("3色5码矩阵 → totalQuantity = 5个值之和")
        void shouldCalculateTotalQuantity() {
            SizeMatrix matrix = new SizeMatrix(1L, standardSizes());
            assertEquals(900, matrix.getTotalQuantity());
        }

        @Test
        @DisplayName("总数量为各尺码数量之和")
        void shouldSumAllSizeQuantities() {
            SizeMatrix matrix = new SizeMatrix(1L, List.of(
                    new SizeEntry(1L, "S", 10),
                    new SizeEntry(2L, "M", 20),
                    new SizeEntry(3L, "L", 30)
            ));
            assertEquals(60, matrix.getTotalQuantity());
        }

        @Test
        @DisplayName("尺码组ID不能为空 → 抛异常")
        void shouldThrowWhenSizeGroupIdIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SizeMatrix(null, standardSizes()));
        }

        @Test
        @DisplayName("尺码列表不能为空 → 抛异常")
        void shouldThrowWhenSizesIsEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SizeMatrix(1L, List.of()));
        }

        @Test
        @DisplayName("尺码列表不能为null → 抛异常")
        void shouldThrowWhenSizesIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SizeMatrix(1L, null));
        }
    }

    @Nested
    @DisplayName("校验")
    class Validation {

        @Test
        @DisplayName("正常矩阵 → 校验通过")
        void shouldPassValidationForNormalMatrix() {
            SizeMatrix matrix = new SizeMatrix(1L, standardSizes());
            assertTrue(matrix.validate());
        }

        @Test
        @DisplayName("重复sizeId → 校验失败")
        void shouldFailValidationForDuplicateSizeId() {
            List<SizeEntry> sizes = List.of(
                    new SizeEntry(10L, "S", 100),
                    new SizeEntry(10L, "M", 200)  // 重复 sizeId
            );
            SizeMatrix matrix = new SizeMatrix(1L, sizes);
            assertFalse(matrix.validate());
        }

        @Test
        @DisplayName("负数数量 → 校验失败")
        void shouldFailValidationForNegativeQuantity() {
            List<SizeEntry> sizes = List.of(
                    new SizeEntry(10L, "S", -1),
                    new SizeEntry(11L, "M", 200)
            );
            SizeMatrix matrix = new SizeMatrix(1L, sizes);
            assertFalse(matrix.validate());
        }

        @Test
        @DisplayName("零数量 → 校验通过")
        void shouldPassValidationForZeroQuantity() {
            SizeMatrix matrix = new SizeMatrix(1L, List.of(
                    new SizeEntry(10L, "S", 0),
                    new SizeEntry(11L, "M", 100)
            ));
            assertTrue(matrix.validate());
        }
    }

    @Nested
    @DisplayName("差异计算")
    class DiffCalculation {

        @Test
        @DisplayName("两个矩阵差异 → 差值为正确差值")
        void shouldCalculateDiffCorrectly() {
            SizeMatrix before = new SizeMatrix(1L, List.of(
                    new SizeEntry(10L, "S", 100),
                    new SizeEntry(11L, "M", 200)
            ));
            SizeMatrix after = new SizeMatrix(1L, List.of(
                    new SizeEntry(10L, "S", 80),
                    new SizeEntry(11L, "M", 250)
            ));

            SizeMatrix diff = before.diff(after);
            assertEquals(1L, diff.getSizeGroupId());
            assertEquals(2, diff.getSizes().size());
            assertEquals(20, diff.getSizes().get(0).getQuantity());   // 100 - 80
            assertEquals(-50, diff.getSizes().get(1).getQuantity());  // 200 - 250
        }

        @Test
        @DisplayName("不同尺码组的矩阵不能计算差异 → 抛异常")
        void shouldThrowWhenDiffDifferentSizeGroup() {
            SizeMatrix m1 = new SizeMatrix(1L, List.of(new SizeEntry(10L, "S", 100)));
            SizeMatrix m2 = new SizeMatrix(2L, List.of(new SizeEntry(20L, "S", 100)));

            assertThrows(IllegalArgumentException.class, () -> m1.diff(m2));
        }
    }

    @Nested
    @DisplayName("SizeEntry 创建")
    class SizeEntryCreation {

        @Test
        @DisplayName("sizeId为null → 抛异常")
        void shouldThrowWhenSizeIdIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SizeEntry(null, "S", 100));
        }

        @Test
        @DisplayName("code为空 → 抛异常")
        void shouldThrowWhenCodeIsBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SizeEntry(1L, "", 100));
            assertThrows(IllegalArgumentException.class,
                    () -> new SizeEntry(1L, "  ", 100));
        }
    }
}
