package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 季节实体
 * <p>
 * 对应数据库表 t_md_season，管理服装行业的经营周期。
 * 季节按年份和类型（春夏/秋冬）划分，同一年份同类型不可重复。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>同一年份同类型季节不可重复</li>
 *   <li>季节编码全局唯一</li>
 *   <li>季节可关闭，关闭后不可在业务单据中选择</li>
 *   <li>季节关闭后其下的波段也不可选用</li>
 * </ul>
 * </p>
 * <p>
 * waves 字段不持久化到数据库（@TableField(exist = false），
 * 仅在查询时由 SeasonDomainService 组装填充。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_season")
public class Season extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 季节编码（如 2026SS），全局唯一 */
    private String code;

    /** 季节名称（如 2026春夏） */
    private String name;

    /** 年份（如 2026） */
    private Integer year;

    /** 季节类型：SPRING_SUMMER/AUTUMN_WINTER */
    private SeasonType seasonType;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 状态：ACTIVE/CLOSED */
    private SeasonStatus status;

    /** 波段列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<Wave> waves = new ArrayList<>();

    /**
     * 添加波段到列表
     *
     * @param wave 波段实体
     */
    public void addWave(Wave wave) {
        this.waves.add(wave);
    }
}
