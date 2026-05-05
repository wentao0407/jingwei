package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 订单转化 — 销售订单→生产订单 入参 DTO
 * <p>
 * 用户在已确认的销售订单中选择若干行，点击"生成生产订单"时提交。
 * 系统按 spuId 分组合并，自动关联 BOM，生成生产订单并建立多对多关联。
 * </p>
 *
 * @author JingWei
 */
@Data
public class ConvertToProductionDTO {

    /** 销售订单ID（必填） */
    @NotNull(message = "销售订单ID不能为空")
    private Long salesOrderId;

    /** 选中的销售订单行列表（必填，至少1行） */
    @NotNull(message = "请选择至少一行订单行")
    @Size(min = 1, message = "请选择至少一行订单行")
    private List<ConvertLineDTO> lines;

    /** 车间ID（可选） */
    private Long workshopId;

    /** 要求完工日期（可选，格式 yyyy-MM-dd） */
    private String deadlineDate;

    /** 备注（可选） */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;
}
