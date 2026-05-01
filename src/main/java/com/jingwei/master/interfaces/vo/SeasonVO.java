package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 季节响应 VO
 * <p>
 * 返回给前端的季节数据，列表查询时不含波段详情，
 * 详情查询时包含关联的波段列表。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class SeasonVO {

    /** 季节ID */
    private Long id;

    /** 季节编码 */
    private String code;

    /** 季节名称 */
    private String name;

    /** 年份 */
    private Integer year;

    /** 季节类型：SPRING_SUMMER/AUTUMN_WINTER */
    private String seasonType;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 状态：ACTIVE/CLOSED */
    private String status;

    /** 波段列表（详情查询时填充，列表查询时为 null） */
    private List<WaveVO> waves;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
