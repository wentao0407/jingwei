package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新季节请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 不允许变更编码（code）和年份类型组合（year + seasonType），
 * 这两者是季节的核心标识，变更后会影响关联业务数据。
 * 如需更换年份类型组合，应关闭当前季节后重新创建。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateSeasonDTO {

    /** 季节名称（可选） */
    @Size(max = 64, message = "季节名称长度不能超过64个字符")
    private String name;

    /** 开始日期（可选） */
    private java.time.LocalDate startDate;

    /** 结束日期（可选） */
    private java.time.LocalDate endDate;
}
