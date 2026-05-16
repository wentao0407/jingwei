package com.jingwei.warehouse.interfaces.controller;

import com.jingwei.common.domain.model.R;
import com.jingwei.warehouse.application.service.PrintApplicationService;
import com.jingwei.warehouse.interfaces.vo.PrintDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 打印 Controller
 * <p>
 * 提供 SKU 标签、入库单、拣货单、出库单、装箱单的打印数据。
 * 返回结构化 JSON，前端负责渲染和调用浏览器打印。
 * </p>
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class PrintController {

    private final PrintApplicationService printApplicationService;

    /**
     * 生成 SKU 标签打印数据
     */
    @PostMapping("/warehouse/print/sku-label")
    public R<PrintDataVO> generateSkuLabel(@RequestParam Long skuId) {
        return R.ok(printApplicationService.generateSkuLabel(skuId));
    }

    /**
     * 生成单据打印数据（通用）
     *
     * @param docType 单据类型：inbound / outbound / pick-list / packing-list
     * @param docId   单据 ID
     */
    @PostMapping("/warehouse/print/doc")
    public R<PrintDataVO> generateDocPrint(@RequestParam String docType,
                                            @RequestParam Long docId) {
        return R.ok(printApplicationService.generateDocPrint(docType, docId));
    }
}
