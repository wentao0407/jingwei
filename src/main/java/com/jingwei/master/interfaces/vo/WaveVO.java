package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 波段响应 VO
 * <p>
 * 返回给前端的波段数据，通常嵌套在 SeasonVO 中。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class WaveVO {

    /** 波段ID */
    private Long id;

    /** 所属季节ID */
    private Long seasonId;

    /** 波段编码 */
    private String code;

    /** 波段名称 */
    private String name;

    /** 交货日期 */
    private LocalDate deliveryDate;

    /** 排序号 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
