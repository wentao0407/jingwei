package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import com.jingwei.common.domain.model.CommonStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * SKU 最小库存单元实体
 * <p>
 * 对应数据库表 t_md_sku，是库存、订单、出入库的最小管理单元。
 * SKU = 颜色 + 尺码，由系统在创建 SPU 时自动交叉生成。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>SKU 编码自动拼接：款式编码-颜色编码-尺码编码</li>
 *   <li>编码冲突时自动追加序号</li>
 *   <li>已被业务引用的 SKU 不可删除，只能停用</li>
 *   <li>每个颜色+尺码组合只允许一个 SKU</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_sku")
public class Sku extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** SKU编码（自动生成，如 SP20260001-BK-M） */
    private String code;

    /** 条码（可自动生成或外部导入） */
    private String barcode;

    /** 款式ID（冗余，方便查询） */
    private Long spuId;

    /** 颜色款ID */
    private Long colorWayId;

    /** 尺码ID */
    private Long sizeId;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 销售价 */
    private BigDecimal salePrice;

    /** 批发价（可选） */
    private BigDecimal wholesalePrice;

    /** 状态：ACTIVE/INACTIVE */
    private CommonStatus status;
}
