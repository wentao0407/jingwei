package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 颜色款实体
 * <p>
 * 对应数据库表 t_md_color_way，表示款式下的一个颜色维度。
 * 每个颜色款下包含多个 SKU（颜色×尺码）。
 * </p>
 * <p>
 * color_code 用于 SKU 编码拼接（如 BK→SP20260001-BK-M），
 * 同一 SPU 内不可重复。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_color_way")
public class ColorWay extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 款式ID */
    private Long spuId;

    /** 颜色名称（如 黑色） */
    private String colorName;

    /** 颜色编码（如 BK，用于SKU编码拼接） */
    private String colorCode;

    /** 潘通色号（可选） */
    private String pantoneCode;

    /** 对应面料ID（可选，不同颜色可能用不同面料） */
    private Long fabricMaterialId;

    /** 颜色款图片URL（可选） */
    private String colorImage;

    /** 排序号 */
    private Integer sortOrder;
}
