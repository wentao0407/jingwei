package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 颜色款响应 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ColorWayVO {

    private Long id;
    private Long spuId;
    private String colorName;
    private String colorCode;
    private String pantoneCode;
    private Long fabricMaterialId;
    private String colorImage;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
