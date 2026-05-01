package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Season;

import java.util.List;

/**
 * 季节仓库接口
 * <p>
 * 提供季节的持久化操作，所有数据库操作通过此接口抽象。
 * </p>
 *
 * @author JingWei
 */
public interface SeasonRepository {

    /**
     * 根据ID查询季节
     *
     * @param id 季节ID
     * @return 季节实体，不存在返回 null
     */
    Season selectById(Long id);

    /**
     * 查询所有季节
     *
     * @return 所有未删除的季节列表，按年份和季节类型排序
     */
    List<Season> selectAll();

    /**
     * 根据年份和类型筛选季节
     *
     * @param year       年份，null 表示不限
     * @param seasonType 季节类型（SPRING_SUMMER/AUTUMN_WINTER），null 表示不限
     * @param status     状态（ACTIVE/CLOSED），null 表示不限
     * @return 符合条件的季节列表
     */
    List<Season> selectByCondition(Integer year, String seasonType, String status);

    /**
     * 检查季节编码是否已存在
     *
     * @param code      季节编码
     * @param excludeId 排除的ID（更新时排除自身），可为 null
     * @return true=已存在
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 检查同一年份同类型季节是否已存在
     *
     * @param year       年份
     * @param seasonType 季节类型
     * @param excludeId  排除的ID（更新时排除自身），可为 null
     * @return true=已存在
     */
    boolean existsByYearAndType(Integer year, String seasonType, Long excludeId);

    /**
     * 插入季节
     *
     * @param season 季节实体
     * @return 影响行数
     */
    int insert(Season season);

    /**
     * 更新季节
     *
     * @param season 季节实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(Season season);

    /**
     * 逻辑删除季节
     *
     * @param id 季节ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
