package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 编码规则段 DTO（嵌套在 Create/Update 规则请求中）
 *
 * @author JingWei
 */
@Getter
@Setter
public class CodingRuleSegmentDTO {

    /** 段类型：FIXED/DATE/SEQUENCE/SEASON/WAREHOUSE/CUSTOM */
    @NotBlank(message = "段类型不能为空")
    private String segmentType;

    /** 段值 */
    private String segmentValue;

    /** 流水号长度（SEQUENCE类型专用） */
    private Integer seqLength;

    /** 流水号重置方式（SEQUENCE类型专用）：NEVER/YEARLY/MONTHLY/DAILY */
    private String seqResetType;

    /** 连接符 */
    private String connector;

    /** 排序号 */
    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;
}
