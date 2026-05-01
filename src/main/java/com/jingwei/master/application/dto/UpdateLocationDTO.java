package com.jingwei.master.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新库位请求 DTO
 * <p>
 * 仅可更新容量和备注，编码和类型不可修改。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateLocationDTO {

    /** 容量（可选） */
    private Integer capacity;

    /** 备注（可选） */
    private String remark;
}
