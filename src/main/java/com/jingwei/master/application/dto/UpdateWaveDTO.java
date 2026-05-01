package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新波段请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 注意：如果所属季节已关闭，不允许修改波段。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateWaveDTO {

    /** 波段名称（可选） */
    @Size(max = 64, message = "波段名称长度不能超过64个字符")
    private String name;

    /** 交货日期（可选） */
    private java.time.LocalDate deliveryDate;

    /** 排序号（可选） */
    private Integer sortOrder;
}
