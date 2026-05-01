package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建季节请求 DTO
 * <p>
 * 入参使用 DTO 而非实体类，遵循开发规范。
 * 创建时 status 默认为 ACTIVE，不需要传入。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateSeasonDTO {

    /** 季节编码（全局唯一，必填，如 2026SS） */
    @NotBlank(message = "季节编码不能为空")
    @Size(max = 16, message = "季节编码长度不能超过16个字符")
    private String code;

    /** 季节名称（必填，如 2026春夏） */
    @NotBlank(message = "季节名称不能为空")
    @Size(max = 64, message = "季节名称长度不能超过64个字符")
    private String name;

    /** 年份（必填，如 2026） */
    @NotNull(message = "年份不能为空")
    private Integer year;

    /** 季节类型：SPRING_SUMMER/AUTUMN_WINTER（必填） */
    @NotBlank(message = "季节类型不能为空")
    private String seasonType;

    /** 开始日期（必填） */
    @NotNull(message = "开始日期不能为空")
    private java.time.LocalDate startDate;

    /** 结束日期（必填） */
    @NotNull(message = "结束日期不能为空")
    private java.time.LocalDate endDate;
}
