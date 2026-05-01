package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Wave;

import java.util.List;

/**
 * 波段仓库接口
 * <p>
 * 提供波段的持久化操作，所有数据库操作通过此接口抽象。
 * 波段从属于季节，不支持独立存在。
 * </p>
 *
 * @author JingWei
 */
public interface WaveRepository {

    /**
     * 根据ID查询波段
     *
     * @param id 波段ID
     * @return 波段实体，不存在返回 null
     */
    Wave selectById(Long id);

    /**
     * 根据季节ID查询所有波段
     *
     * @param seasonId 季节ID
     * @return 该季节下的所有未删除波段，按 sort_order 排序
     */
    List<Wave> selectBySeasonId(Long seasonId);

    /**
     * 检查同一季节内编码是否已存在
     *
     * @param seasonId  季节ID
     * @param code      波段编码
     * @param excludeId 排除的ID（更新时排除自身），可为 null
     * @return true=已存在
     */
    boolean existsBySeasonIdAndCode(Long seasonId, String code, Long excludeId);

    /**
     * 获取季节内的最大排序号
     *
     * @param seasonId 季节ID
     * @return 最大排序号，无波段时返回 0
     */
    int getMaxSortOrder(Long seasonId);

    /**
     * 插入波段
     *
     * @param wave 波段实体
     * @return 影响行数
     */
    int insert(Wave wave);

    /**
     * 更新波段
     *
     * @param wave 波段实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(Wave wave);

    /**
     * 逻辑删除波段
     *
     * @param id 波段ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 统计季节内的波段数量
     *
     * @param seasonId 季节ID
     * @return 波段数量
     */
    long countBySeasonId(Long seasonId);
}
