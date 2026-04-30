package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import com.jingwei.system.domain.model.UserStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 编码规则实体
 * <p>
 * 对应数据库表 t_md_coding_rule，定义业务单据的编码生成规则。
 * 一条规则由多个段（CodingRuleSegment）按 sort_order 排序拼接而成。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_coding_rule")
public class CodingRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 规则编码（如 SALES_ORDER） */
    private String code;

    /** 规则名称 */
    private String name;

    /** 业务类型 */
    private String businessType;

    /** 说明 */
    private String description;

    /** 状态：ACTIVE/INACTIVE */
    private UserStatus status;

    /** 是否已使用（已使用的规则不可删除，只能停用） */
    private Boolean used;
}
