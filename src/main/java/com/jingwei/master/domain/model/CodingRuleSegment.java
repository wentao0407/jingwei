package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 编码规则段实体
 * <p>
 * 对应数据库表 t_md_coding_rule_segment，定义编码规则中每个段的类型和配置。
 * 多个段按 sort_order 排序拼接，生成最终编码。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_coding_rule_segment")
public class CodingRuleSegment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 段类型：FIXED/DATE/SEQUENCE/SEASON/WAREHOUSE/CUSTOM */
    private SegmentType segmentType;

    /** 段值（FIXED=固定文本，DATE=日期格式如YYYYMM，CUSTOM=上下文key） */
    private String segmentValue;

    /** 流水号长度（SEQUENCE类型专用，如 4 表示补零到4位） */
    private Integer seqLength;

    /** 流水号重置方式（SEQUENCE类型专用）：NEVER/YEARLY/MONTHLY/DAILY */
    private SeqResetType seqResetType;

    /** 连接符（拼接时本段与前段之间，如 "-" 或 ""） */
    private String connector;

    /** 排序号 */
    private Integer sortOrder;
}
