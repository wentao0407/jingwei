package com.jingwei.master.application.service;

import com.jingwei.master.application.dto.AddColorDTO;
import com.jingwei.master.application.dto.BatchUpdateSkuPriceDTO;
import com.jingwei.master.application.dto.ColorItemDTO;
import com.jingwei.master.application.dto.CreateSpuDTO;
import com.jingwei.master.application.dto.UpdateSkuPriceDTO;
import com.jingwei.master.application.dto.UpdateSpuDTO;
import com.jingwei.master.domain.model.ColorWay;
import com.jingwei.master.domain.model.Sku;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.model.SpuStatus;
import com.jingwei.master.domain.service.SpuDomainService;
import com.jingwei.master.interfaces.vo.ColorWayVO;
import com.jingwei.master.interfaces.vo.SkuVO;
import com.jingwei.master.interfaces.vo.SpuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 款式应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuApplicationService {

    private final SpuDomainService spuDomainService;

    /**
     * 创建款式（含自动生成 Color-way 和 SKU）
     */
    @Transactional(rollbackFor = Exception.class)
    public SpuVO createSpu(CreateSpuDTO dto) {
        Spu spu = new Spu();
        spu.setCode(dto.getCode());
        spu.setName(dto.getName());
        spu.setSeasonId(dto.getSeasonId());
        spu.setCategoryId(dto.getCategoryId());
        spu.setBrandId(dto.getBrandId());
        spu.setSizeGroupId(dto.getSizeGroupId());
        spu.setDesignImage(dto.getDesignImage());
        spu.setRemark(dto.getRemark());

        List<ColorWay> colorWays = dto.getColors().stream()
                .map(this::toColorWay)
                .toList();

        Spu saved = spuDomainService.createSpu(spu, colorWays);
        return toSpuVO(saved);
    }

    /**
     * 更新款式
     */
    @Transactional(rollbackFor = Exception.class)
    public SpuVO updateSpu(Long spuId, UpdateSpuDTO dto) {
        Spu updated = spuDomainService.updateSpu(
                spuId, dto.getName(), dto.getSeasonId(), dto.getCategoryId(),
                dto.getBrandId(), dto.getDesignImage(), dto.getStatus(), dto.getRemark());
        return toSpuVO(updated);
    }

    /**
     * 删除款式
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpu(Long spuId) {
        spuDomainService.deleteSpu(spuId);
    }

    /**
     * 查询款式列表
     */
    public List<SpuVO> listSpus(String status, Long seasonId, Long categoryId) {
        List<Spu> spus = spuDomainService.listSpus(status, seasonId, categoryId);
        return spus.stream().map(this::toSpuVO).toList();
    }

    /**
     * 查询款式详情（含颜色款和SKU）
     */
    public SpuVO getSpuDetail(Long spuId) {
        Spu spu = spuDomainService.getSpuDetail(spuId);
        return toSpuVOWithDetails(spu);
    }

    /**
     * 追加颜色
     */
    @Transactional(rollbackFor = Exception.class)
    public SpuVO addColors(Long spuId, AddColorDTO dto) {
        List<ColorWay> newColors = dto.getColors().stream()
                .map(this::toColorWay)
                .toList();
        spuDomainService.addColors(spuId, newColors);
        // 返回更新后的款式详情
        return getSpuDetail(spuId);
    }

    /**
     * 更新 SKU 价格
     */
    @Transactional(rollbackFor = Exception.class)
    public SkuVO updateSkuPrice(UpdateSkuPriceDTO dto) {
        Sku updated = spuDomainService.updateSkuPrice(
                dto.getSkuId(), dto.getCostPrice(), dto.getSalePrice(), dto.getWholesalePrice());
        return toSkuVO(updated);
    }

    /**
     * 批量更新 SKU 价格
     * <p>
     * 支持按 SPU 维度或按颜色款维度批量更新价格。
     * </p>
     *
     * @param dto 批量更新请求
     * @return 更新的 SKU 总行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSkuPrice(BatchUpdateSkuPriceDTO dto) {
        return spuDomainService.batchUpdateSkuPrice(
                dto.getSpuId(), dto.getColorWayId(),
                dto.getCostPrice(), dto.getSalePrice(), dto.getWholesalePrice());
    }

    /**
     * 停用 SKU
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateSku(Long skuId) {
        spuDomainService.deactivateSku(skuId);
    }

    // ==================== 转换方法 ====================

    private ColorWay toColorWay(ColorItemDTO dto) {
        ColorWay cw = new ColorWay();
        cw.setColorName(dto.getColorName());
        cw.setColorCode(dto.getColorCode());
        cw.setPantoneCode(dto.getPantoneCode());
        cw.setFabricMaterialId(dto.getFabricMaterialId());
        cw.setColorImage(dto.getColorImage());
        return cw;
    }

    private SpuVO toSpuVO(Spu spu) {
        SpuVO vo = new SpuVO();
        vo.setId(spu.getId());
        vo.setCode(spu.getCode());
        vo.setName(spu.getName());
        vo.setSeasonId(spu.getSeasonId());
        vo.setCategoryId(spu.getCategoryId());
        vo.setBrandId(spu.getBrandId());
        vo.setSizeGroupId(spu.getSizeGroupId());
        vo.setDesignImage(spu.getDesignImage());
        vo.setStatus(spu.getStatus().name());
        vo.setRemark(spu.getRemark());
        vo.setCreatedAt(spu.getCreatedAt());
        vo.setUpdatedAt(spu.getUpdatedAt());
        return vo;
    }

    private SpuVO toSpuVOWithDetails(Spu spu) {
        SpuVO vo = toSpuVO(spu);
        if (spu.getColorWays() != null) {
            vo.setColorWays(spu.getColorWays().stream().map(this::toColorWayVO).toList());
        }
        if (spu.getSkus() != null) {
            vo.setSkus(spu.getSkus().stream().map(this::toSkuVO).toList());
        }
        return vo;
    }

    private ColorWayVO toColorWayVO(ColorWay cw) {
        ColorWayVO vo = new ColorWayVO();
        vo.setId(cw.getId());
        vo.setSpuId(cw.getSpuId());
        vo.setColorName(cw.getColorName());
        vo.setColorCode(cw.getColorCode());
        vo.setPantoneCode(cw.getPantoneCode());
        vo.setFabricMaterialId(cw.getFabricMaterialId());
        vo.setColorImage(cw.getColorImage());
        vo.setSortOrder(cw.getSortOrder());
        vo.setCreatedAt(cw.getCreatedAt());
        vo.setUpdatedAt(cw.getUpdatedAt());
        return vo;
    }

    private SkuVO toSkuVO(Sku sku) {
        SkuVO vo = new SkuVO();
        vo.setId(sku.getId());
        vo.setCode(sku.getCode());
        vo.setBarcode(sku.getBarcode());
        vo.setSpuId(sku.getSpuId());
        vo.setColorWayId(sku.getColorWayId());
        vo.setSizeId(sku.getSizeId());
        vo.setCostPrice(sku.getCostPrice());
        vo.setSalePrice(sku.getSalePrice());
        vo.setWholesalePrice(sku.getWholesalePrice());
        vo.setStatus(sku.getStatus().name());
        vo.setCreatedAt(sku.getCreatedAt());
        vo.setUpdatedAt(sku.getUpdatedAt());
        return vo;
    }
}
