package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.ColorWay;
import com.jingwei.master.domain.model.Size;
import com.jingwei.master.domain.model.SizeGroup;
import com.jingwei.master.domain.model.Sku;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.model.SpuStatus;
import com.jingwei.master.domain.repository.ColorWayRepository;
import com.jingwei.master.domain.repository.SizeGroupRepository;
import com.jingwei.master.domain.repository.SizeRepository;
import com.jingwei.master.domain.repository.SkuRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 款式领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>SPU CRUD 及业务校验</li>
 *   <li>创建 SPU 时自动按颜色×尺码交叉生成 SKU</li>
 *   <li>追加颜色时增量生成 SKU</li>
 *   <li>SKU 编码自动拼接及冲突处理</li>
 * </ul>
 * </p>
 * <p>
 * SKU 自动生成规则：
 * <ol>
 *   <li>为每个颜色创建 Color-way 记录</li>
 *   <li>查询尺码组下所有尺码</li>
 *   <li>按 颜色×尺码 交叉生成 SKU</li>
 *   <li>SKU 编码格式：款式编码-颜色编码-尺码编码</li>
 *   <li>编码冲突时自动追加序号（如 SP20260001-BK-M-2）</li>
 * </ol>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuDomainService {

    private final SpuRepository spuRepository;
    private final ColorWayRepository colorWayRepository;
    private final SkuRepository skuRepository;
    private final SizeGroupRepository sizeGroupRepository;
    private final SizeRepository sizeRepository;

    // ==================== SPU CRUD ====================

    /**
     * 创建款式（含自动生成 Color-way 和 SKU）
     * <p>
     * 流程：
     * <ol>
     *   <li>校验款式编码唯一性</li>
     *   <li>校验尺码组存在</li>
     *   <li>校验颜色列表非空</li>
     *   <li>校验同一 SPU 内颜色编码不重复</li>
     *   <li>创建 SPU 记录（状态 DRAFT）</li>
     *   <li>为每个颜色创建 Color-way 记录</li>
     *   <li>按 颜色×尺码 交叉生成 SKU 记录</li>
     * </ol>
     * </p>
     *
     * @param spu        款式实体
     * @param colorItems 颜色项列表（colorName, colorCode）
     * @return 保存后的款式实体
     */
    public Spu createSpu(Spu spu, List<ColorWay> colorItems) {
        // 校验编码唯一性
        if (spuRepository.existsByCode(spu.getCode(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "款式编码已存在");
        }

        // 校验尺码组存在
        SizeGroup sizeGroup = sizeGroupRepository.selectById(spu.getSizeGroupId());
        if (sizeGroup == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "尺码组不存在");
        }

        // 校验颜色列表非空
        if (colorItems == null || colorItems.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "至少选择一个颜色");
        }

        // 校验颜色编码不重复
        long distinctCount = colorItems.stream()
                .map(ColorWay::getColorCode)
                .distinct()
                .count();
        if (distinctCount < colorItems.size()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "颜色编码不能重复");
        }

        // 创建 SPU
        spu.setStatus(SpuStatus.DRAFT);
        try {
            spuRepository.insert(spu);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建款式触发唯一约束: code={}", spu.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "款式编码已存在");
        }

        // 校验尺码组下有尺码
        List<Size> sizes = sizeRepository.selectBySizeGroupId(spu.getSizeGroupId());
        if (sizes.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "所选尺码组下没有尺码，请先配置尺码");
        }

        // 生成 Color-way 和 SKU
        generateColorWaysAndSkus(spu, colorItems, sizes);

        log.info("创建款式: code={}, name={}, colors={}, sizes={}, total SKUs={}",
                spu.getCode(), spu.getName(), colorItems.size(), sizes.size(),
                colorItems.size() * sizes.size());
        return spu;
    }

    /**
     * 更新款式
     * <p>
     * 不允许变更编码和尺码组。
     * </p>
     */
    public Spu updateSpu(Long spuId, String name, Long seasonId, Long categoryId,
                         Long brandId, String designImage, String status, String remark) {
        Spu existing = spuRepository.selectById(spuId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "款式不存在");
        }

        if (name != null) {
            existing.setName(name);
        }
        if (seasonId != null) {
            existing.setSeasonId(seasonId);
        }
        if (categoryId != null) {
            existing.setCategoryId(categoryId);
        }
        if (brandId != null) {
            existing.setBrandId(brandId);
        }
        if (designImage != null) {
            existing.setDesignImage(designImage);
        }
        if (status != null) {
            existing.setStatus(SpuStatus.valueOf(status));
        }
        if (remark != null) {
            existing.setRemark(remark);
        }

        int rows = spuRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新款式: id={}", spuId);
        return spuRepository.selectById(spuId);
    }

    /**
     * 删除款式
     * <p>
     * 同时删除关联的 Color-way 和 SKU。
     * 已被业务引用的 SKU 不可删除。
     * </p>
     */
    public void deleteSpu(Long spuId) {
        Spu existing = spuRepository.selectById(spuId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "款式不存在");
        }

        // 检查 SKU 是否被业务引用（当前库存/订单模块未实现，预留钩子）
        long referencedSkuCount = countReferencedSkus(spuId);
        if (referencedSkuCount > 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该款式下有" + referencedSkuCount + "个SKU已被业务引用，不可删除");
        }

        // 先删 SKU，再删 Color-way，最后删 SPU
        List<Sku> skus = skuRepository.selectBySpuId(spuId);
        for (Sku sku : skus) {
            skuRepository.deleteById(sku.getId());
        }

        List<ColorWay> colorWays = colorWayRepository.selectBySpuId(spuId);
        for (ColorWay cw : colorWays) {
            colorWayRepository.deleteById(cw.getId());
        }

        spuRepository.deleteById(spuId);
        log.info("删除款式及{}个颜色款、{}个SKU: id={}, code={}",
                colorWays.size(), skus.size(), spuId, existing.getCode());
    }

    /**
     * 查询款式列表
     */
    public List<Spu> listSpus(String status, Long seasonId, Long categoryId) {
        return spuRepository.selectByCondition(status, seasonId, categoryId);
    }

    /**
     * 查询款式详情（含颜色款和SKU列表）
     */
    public Spu getSpuDetail(Long spuId) {
        Spu spu = spuRepository.selectById(spuId);
        if (spu == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "款式不存在");
        }

        spu.setColorWays(colorWayRepository.selectBySpuId(spuId));
        spu.setSkus(skuRepository.selectBySpuId(spuId));
        return spu;
    }

    // ==================== 追加颜色 ====================

    /**
     * 为款式追加新颜色
     * <p>
     * 只为新增颜色生成 SKU，不影响已有颜色和 SKU。
     * </p>
     *
     * @param spuId       款式ID
     * @param newColors   新增颜色列表
     */
    public void addColors(Long spuId, List<ColorWay> newColors) {
        Spu existing = spuRepository.selectById(spuId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "款式不存在");
        }

        // 校验新颜色编码不与已有颜色重复
        for (ColorWay color : newColors) {
            if (colorWayRepository.existsBySpuIdAndColorCode(spuId, color.getColorCode(), null)) {
                throw new BizException(ErrorCode.DATA_ALREADY_EXISTS,
                        "颜色编码 " + color.getColorCode() + " 已存在");
            }
        }

        // 校验新颜色编码之间不重复
        long distinctCount = newColors.stream()
                .map(ColorWay::getColorCode)
                .distinct()
                .count();
        if (distinctCount < newColors.size()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "新增颜色编码不能重复");
        }

        // 查询尺码组下所有尺码
        List<Size> sizes = sizeRepository.selectBySizeGroupId(existing.getSizeGroupId());
        generateColorWaysAndSkus(existing, newColors, sizes);

        log.info("追加颜色: spuId={}, newColors={}, newSKUs={}",
                spuId, newColors.size(), newColors.size() * sizes.size());
    }

    // ==================== SKU 管理 ====================

    /**
     * 更新单个 SKU 价格
     */
    public Sku updateSkuPrice(Long skuId, BigDecimal costPrice, BigDecimal salePrice, BigDecimal wholesalePrice) {
        Sku existing = skuRepository.selectById(skuId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "SKU不存在");
        }

        if (costPrice != null) {
            existing.setCostPrice(costPrice);
        }
        if (salePrice != null) {
            existing.setSalePrice(salePrice);
        }
        if (wholesalePrice != null) {
            existing.setWholesalePrice(wholesalePrice);
        }

        int rows = skuRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新SKU价格: id={}", skuId);
        return skuRepository.selectById(skuId);
    }

    /**
     * 停用 SKU
     * <p>
     * 已被业务引用的 SKU 不可删除，只能停用。
     * </p>
     */
    public void deactivateSku(Long skuId) {
        Sku existing = skuRepository.selectById(skuId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "SKU不存在");
        }

        if (existing.getStatus() == CommonStatus.INACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "SKU已停用");
        }

        existing.setStatus(CommonStatus.INACTIVE);
        int rows = skuRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("停用SKU: id={}, code={}", skuId, existing.getCode());
    }

    // ==================== 批量价格更新 ====================

    /**
     * 批量更新 SKU 价格
     * <p>
     * 支持两种模式：
     * <ul>
     *   <li>按 SPU 维度：colorWayId 为空时，更新该款式下所有 SKU</li>
     *   <li>按颜色款维度：colorWayId 非空时，仅更新该颜色下所有 SKU</li>
     * </ul>
     * 只更新非空的价格字段，至少需要传入一个价格字段。
     * </p>
     *
     * @param spuId          款式ID
     * @param colorWayId     颜色款ID（可选，为空则按 SPU 维度更新）
     * @param costPrice      成本价（可选）
     * @param salePrice      销售价（可选）
     * @param wholesalePrice 批发价（可选）
     * @return 更新的 SKU 总行数
     */
    public int batchUpdateSkuPrice(Long spuId, Long colorWayId,
                                   BigDecimal costPrice, BigDecimal salePrice, BigDecimal wholesalePrice) {
        // 校验至少传入一个价格
        if (costPrice == null && salePrice == null && wholesalePrice == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "至少传入一个价格字段");
        }

        // 校验 SPU 存在
        Spu spu = spuRepository.selectById(spuId);
        if (spu == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "款式不存在");
        }

        // 如果指定了颜色款，校验颜色款存在且属于该 SPU
        if (colorWayId != null) {
            ColorWay colorWay = colorWayRepository.selectById(colorWayId);
            if (colorWay == null) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND, "颜色款不存在");
            }
            if (!colorWay.getSpuId().equals(spuId)) {
                throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "颜色款不属于该款式");
            }
        }

        // 按价格字段逐个执行批量更新，累加影响行数
        int totalRows = 0;
        if (costPrice != null) {
            totalRows += doBatchUpdatePrice(spuId, colorWayId, "costPrice", costPrice);
        }
        if (salePrice != null) {
            totalRows += doBatchUpdatePrice(spuId, colorWayId, "salePrice", salePrice);
        }
        if (wholesalePrice != null) {
            totalRows += doBatchUpdatePrice(spuId, colorWayId, "wholesalePrice", wholesalePrice);
        }

        String scope = colorWayId != null ? "颜色款" + colorWayId : "款式" + spuId;
        log.info("批量更新SKU价格: scope={}, costPrice={}, salePrice={}, wholesalePrice={}, 更新行数={}",
                scope, costPrice, salePrice, wholesalePrice, totalRows);
        return totalRows;
    }

    /**
     * 执行单种价格类型的批量更新
     * <p>
     * 根据 colorWayId 是否为空，选择按 SPU 或按颜色款维度更新。
     * </p>
     *
     * @param spuId     款式ID
     * @param colorWayId 颜色款ID（为空则按 SPU 维度）
     * @param priceType 价格类型：costPrice/salePrice/wholesalePrice
     * @param price     价格值
     * @return 更新行数
     */
    private int doBatchUpdatePrice(Long spuId, Long colorWayId, String priceType, BigDecimal price) {
        if (colorWayId != null) {
            return skuRepository.batchUpdatePriceByColorWay(colorWayId, priceType, price);
        }
        return skuRepository.batchUpdatePrice(spuId, priceType, price);
    }

    // ==================== 私有方法 ====================

    /**
     * 为指定 SPU 和颜色列表生成 Color-way 和 SKU
     * <p>
     * 核心逻辑：
     * <ol>
     *   <li>遍历每个颜色，创建 Color-way 记录</li>
     *   <li>遍历每个颜色×每个尺码，创建 SKU 记录</li>
     *   <li>SKU 编码 = SPU编码-颜色编码-尺码编码</li>
     *   <li>编码冲突时追加序号</li>
     * </ol>
     * </p>
     *
     * @param spu        款式实体（已有 id 和 code）
     * @param colors     颜色列表
     * @param sizes      尺码列表
     */
    private void generateColorWaysAndSkus(Spu spu, List<ColorWay> colors, List<Size> sizes) {
        int sortOrder = (int) colorWayRepository.countBySpuId(spu.getId());

        for (ColorWay color : colors) {
            // 创建 Color-way
            color.setSpuId(spu.getId());
            color.setSortOrder(sortOrder++);
            colorWayRepository.insert(color);

            // 为该颜色×所有尺码生成 SKU
            for (Size size : sizes) {
                Sku sku = new Sku();
                sku.setSpuId(spu.getId());
                sku.setColorWayId(color.getId());
                sku.setSizeId(size.getId());
                sku.setStatus(CommonStatus.ACTIVE);

                // 拼接 SKU 编码：款式编码-颜色编码-尺码编码
                String skuCode = spu.getCode() + "-" + color.getColorCode() + "-" + size.getCode();
                skuCode = resolveCodeConflict(skuCode);
                sku.setCode(skuCode);

                try {
                    skuRepository.insert(sku);
                } catch (DuplicateKeyException e) {
                    // 数据库唯一索引兜底，追加序号重试
                    skuCode = spu.getCode() + "-" + color.getColorCode() + "-" + size.getCode();
                    skuCode = resolveCodeConflict(skuCode);
                    sku.setCode(skuCode);
                    skuRepository.insert(sku);
                }
            }
        }
    }

    /**
     * 解决 SKU 编码冲突
     * <p>
     * 如果编码已被占用，自动追加序号（-2, -3, ...）直到找到可用编码。
     * 这种情况极少发生，通常只在数据迁移或特殊编码规则下才会触发。
     * </p>
     *
     * @param baseCode 基础编码（如 SP20260001-BK-M）
     * @return 可用的编码
     */
    private String resolveCodeConflict(String baseCode) {
        if (!skuRepository.existsByCode(baseCode)) {
            return baseCode;
        }

        // 追加序号，从 2 开始
        int seq = 2;
        while (skuRepository.existsByCode(baseCode + "-" + seq)) {
            seq++;
        }
        log.info("SKU编码冲突，追加序号: {} -> {}", baseCode, baseCode + "-" + seq);
        return baseCode + "-" + seq;
    }

    /**
     * 统计被业务引用的 SKU 数量
     * <p>
     * 当前库存/订单模块尚未实现，返回 0。
     * </p>
     */
    private long countReferencedSkus(Long spuId) {
        // TODO: 库存/订单模块实现后，替换为真实查询
        return 0;
    }
}
