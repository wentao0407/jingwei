package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 编码规则段VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CodingRuleSegmentVO {

    /** 段ID */
    private Long id;

    /** 段类型 */
    private String segmentType;

    /** 段值 */
    private String segmentValue;

    /** 流水号长度 */
    private Integer seqLength;

    /** 流水号重置方式 */
    private String seqResetType;

    /** 连接符 */
    private String connector;

    /** 排序号 */
    private Integer sortOrder;
}
