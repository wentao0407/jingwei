package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 生成编码请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class GenerateCodeDTO {

    /** 规则编码 */
    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    /** 上下文变量（如仓库编码、季节编码、自定义值等） */
    private Map<String, String> context;
}
