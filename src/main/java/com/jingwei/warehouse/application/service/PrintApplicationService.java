package com.jingwei.warehouse.application.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Sku;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.repository.SkuRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import com.jingwei.warehouse.interfaces.vo.PrintDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印应用服务
 * <p>
 * 生成各类打印数据（SKU标签、入库单、拣货单、出库单、装箱单），
 * 返回结构化 JSON，前端负责渲染和调用浏览器打印。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrintApplicationService {

    private final SkuRepository skuRepository;
    private final SpuRepository spuRepository;

    /**
     * 生成 SKU 标签打印数据
     *
     * @param skuId SKU ID
     * @return 标签打印数据
     */
    public PrintDataVO generateSkuLabel(Long skuId) {
        Sku sku = skuRepository.selectById(skuId);
        if (sku == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "SKU 不存在");
        }
        Spu spu = spuRepository.selectById(sku.getSpuId());

        PrintDataVO vo = new PrintDataVO();
        vo.setTitle("SKU 标签");
        vo.setDocNo(sku.getCode());

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("SKU编码", sku.getCode());
        fields.put("款式编码", spu != null ? spu.getCode() : "");
        fields.put("款式名称", spu != null ? spu.getName() : "");
        fields.put("条码", sku.getCode());
        vo.setFields(fields);

        vo.setLines(List.of());

        log.info("生成 SKU 标签打印数据: skuId={}", skuId);
        return vo;
    }

    /**
     * 生成入库单打印数据（基础骨架，前端传入单据 ID 后由前端自行获取行明细）
     */
    public PrintDataVO generateDocPrint(String docType, Long docId) {
        PrintDataVO vo = new PrintDataVO();
        vo.setTitle(switch (docType) {
            case "inbound" -> "入库单";
            case "outbound" -> "出库单";
            case "pick-list" -> "拣货单";
            case "packing-list" -> "装箱单";
            default -> "单据";
        });
        vo.setDocNo(String.valueOf(docId));
        vo.setFields(Map.of("单据ID", String.valueOf(docId)));
        vo.setLines(List.of());

        log.info("生成{}打印数据: docType={}, docId={}", vo.getTitle(), docType, docId);
        return vo;
    }
}
