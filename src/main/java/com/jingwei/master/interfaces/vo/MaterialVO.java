package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 物料主数据 VO
 * <p>
 * 返回给前端的物料详情视图对象。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class MaterialVO {

    /** 物料ID */
    private Long id;

    /** 物料编码 */
    private String code;

    /** 物料名称 */
    private String name;

    /** 物料类型：FABRIC/TRIM/PACKAGING */
    private String type;

    /** 物料分类ID */
    private Long categoryId;

    /** 基本单位 */
    private String unit;

    /** 状态：ACTIVE/INACTIVE */
    private String status;

    /** 扩展属性（JSONB） */
    private Map<String, Object> extAttrs;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
