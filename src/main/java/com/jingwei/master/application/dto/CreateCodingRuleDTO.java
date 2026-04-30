package com.jingwei.master.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建编码规则请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateCodingRuleDTO {

    /** 规则编码 */
    @NotBlank(message = "规则编码不能为空")
    private String code;

    /** 规则名称 */
    @NotBlank(message = "规则名称不能为空")
    private String name;

    /** 业务类型 */
    private String businessType;

    /** 说明 */
    private String description;

    /** 段列表 */
    @NotEmpty(message = "段列表不能为空")
    @Valid
    private List<CodingRuleSegmentDTO> segments;
}
