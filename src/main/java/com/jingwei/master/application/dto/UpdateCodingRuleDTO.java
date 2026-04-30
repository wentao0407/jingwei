package com.jingwei.master.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新编码规则请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateCodingRuleDTO {

    /** 规则名称 */
    private String name;

    /** 业务类型 */
    private String businessType;

    /** 说明 */
    private String description;

    /** 状态：ACTIVE/INACTIVE */
    private String status;
}
