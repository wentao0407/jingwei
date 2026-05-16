package com.jingwei.warehouse.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 打印数据 VO
 * <p>
 * 通用打印数据结构，前端根据 title 和字段渲染打印模板。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class PrintDataVO {

    /** 打印标题（如：SKU标签、入库单、拣货单） */
    private String title;

    /** 单据编号 */
    private String docNo;

    /** 头信息字段（key=标签，value=值） */
    private Map<String, String> fields;

    /** 行项目列表（每行是一个 Map） */
    private List<Map<String, String>> lines;
}
