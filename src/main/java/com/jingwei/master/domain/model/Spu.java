package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * SPU 款式实体
 * <p>
 * 对应数据库表 t_md_spu，管理款式的共性信息。
 * SPU 是三层结构的顶层：SPU → ColorWay → SKU。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>创建 SPU 时必须选择尺码组，创建后不可更换</li>
 *   <li>创建 SPU 时至少选择一个颜色，系统自动按颜色×尺码交叉生成 SKU</li>
 *   <li>SPU 创建后可追加新颜色，系统增量生成对应 SKU</li>
 *   <li>SKU 编码自动拼接：款式编码-颜色编码-尺码编码</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_spu")
public class Spu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 款式编码（如 SP20260001），全局唯一 */
    private String code;

    /** 款式名称 */
    private String name;

    /** 季节ID */
    private Long seasonId;

    /** 品类ID */
    private Long categoryId;

    /** 品牌ID（可选） */
    private Long brandId;

    /** 尺码组ID（创建后不可更换） */
    private Long sizeGroupId;

    /** 款式图URL */
    private String designImage;

    /** 默认BOM ID（可选） */
    private Long defaultBomId;

    /** 状态：DRAFT/ACTIVE/INACTIVE */
    private SpuStatus status;

    /** 备注 */
    private String remark;

    /** 颜色款列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<ColorWay> colorWays = new ArrayList<>();

    /** SKU列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<Sku> skus = new ArrayList<>();
}
