package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 波段实体
 * <p>
 * 对应数据库表 t_md_wave，从属于季节，用于精细化排期。
 * 波段为可选功能，不使用波段不影响其他业务。
 * </p>
 * <p>
 * 典型波段示例：
 * <ul>
 *   <li>春一（2月中旬交货）、春二（3月中旬交货）</li>
 *   <li>秋一（8月中旬交货）、冬一（10月中旬交货）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_wave")
public class Wave extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属季节ID */
    private Long seasonId;

    /** 波段编码（如 2026SS-W1），组内唯一 */
    private String code;

    /** 波段名称（如 春一） */
    private String name;

    /** 交货日期 */
    private LocalDate deliveryDate;

    /** 排序号（值越小越靠前） */
    private Integer sortOrder;
}
