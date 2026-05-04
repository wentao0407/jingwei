package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * MRP 计算结果包装 VO
 * <p>
 * 包含计算结果列表和计算过程中的警告信息。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class MrpCalculateResultVO {

    /** 计算结果列表 */
    private List<MrpResultVO> results;

    /** 计算结果条数 */
    private int totalItems;

    /** 警告信息（如无BOM跳过、无数量跳过等） */
    private List<String> warnings;
}
