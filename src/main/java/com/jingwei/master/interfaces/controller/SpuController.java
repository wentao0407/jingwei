package com.jingwei.master.interfaces.controller;

import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.AddColorDTO;
import com.jingwei.master.application.dto.BatchUpdateSkuPriceDTO;
import com.jingwei.master.application.dto.CreateSpuDTO;
import com.jingwei.master.application.dto.UpdateSkuPriceDTO;
import com.jingwei.master.application.dto.UpdateSpuDTO;
import com.jingwei.master.application.service.SpuApplicationService;
import com.jingwei.master.interfaces.vo.SkuVO;
import com.jingwei.master.interfaces.vo.SpuVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 款式管理 Controller
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SpuController {

    private final SpuApplicationService spuApplicationService;

    /**
     * 创建款式（含自动生成颜色款和SKU）
     */
    @PostMapping("/master/spu/create")
    public R<SpuVO> createSpu(@Valid @RequestBody CreateSpuDTO dto) {
        return R.ok(spuApplicationService.createSpu(dto));
    }

    /**
     * 更新款式
     */
    @PostMapping("/master/spu/update")
    public R<SpuVO> updateSpu(@RequestParam Long spuId,
                              @Valid @RequestBody UpdateSpuDTO dto) {
        return R.ok(spuApplicationService.updateSpu(spuId, dto));
    }

    /**
     * 删除款式
     */
    @PostMapping("/master/spu/delete")
    public R<Void> deleteSpu(@RequestParam Long spuId) {
        spuApplicationService.deleteSpu(spuId);
        return R.ok();
    }

    /**
     * 查询款式列表
     */
    @PostMapping("/master/spu/list")
    public R<List<SpuVO>> listSpus(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) Long seasonId,
                                   @RequestParam(required = false) Long categoryId) {
        return R.ok(spuApplicationService.listSpus(status, seasonId, categoryId));
    }

    /**
     * 查询款式详情（含颜色款和SKU）
     */
    @PostMapping("/master/spu/detail")
    public R<SpuVO> getSpuDetail(@RequestParam Long spuId) {
        return R.ok(spuApplicationService.getSpuDetail(spuId));
    }

    /**
     * 追加颜色（增量生成SKU）
     */
    @PostMapping("/master/spu/addColor")
    public R<SpuVO> addColors(@RequestParam Long spuId,
                              @Valid @RequestBody AddColorDTO dto) {
        return R.ok(spuApplicationService.addColors(spuId, dto));
    }

    /**
     * 更新SKU价格
     */
    @PostMapping("/master/sku/updatePrice")
    public R<SkuVO> updateSkuPrice(@Valid @RequestBody UpdateSkuPriceDTO dto) {
        return R.ok(spuApplicationService.updateSkuPrice(dto));
    }

    /**
     * 批量更新SKU价格
     * <p>
     * 支持按款式（SPU）维度或按颜色款维度批量更新价格，
     * 传入 colorWayId 则只更新该颜色下所有 SKU，不传则更新该款式下所有 SKU。
     * </p>
     */
    @PostMapping("/master/sku/batchUpdatePrice")
    public R<Integer> batchUpdateSkuPrice(@Valid @RequestBody BatchUpdateSkuPriceDTO dto) {
        return R.ok(spuApplicationService.batchUpdateSkuPrice(dto));
    }

    /**
     * 停用SKU
     */
    @PostMapping("/master/sku/deactivate")
    public R<Void> deactivateSku(@RequestParam Long skuId) {
        spuApplicationService.deactivateSku(skuId);
        return R.ok();
    }
}
