package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 款式响应 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class SpuVO {

    private Long id;
    private String code;
    private String name;
    private Long seasonId;
    private Long categoryId;
    private Long brandId;
    private Long sizeGroupId;
    private String designImage;
    private String status;
    private String remark;
    private List<ColorWayVO> colorWays;
    private List<SkuVO> skus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
