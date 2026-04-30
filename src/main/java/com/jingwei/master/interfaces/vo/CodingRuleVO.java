package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 编码规则VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CodingRuleVO {

    /** 规则ID */
    private Long id;

    /** 规则编码 */
    private String code;

    /** 规则名称 */
    private String name;

    /** 业务类型 */
    private String businessType;

    /** 说明 */
    private String description;

    /** 状态 */
    private String status;

    /** 是否已使用 */
    private Boolean used;

    /** 段列表 */
    private List<CodingRuleSegmentVO> segments;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
