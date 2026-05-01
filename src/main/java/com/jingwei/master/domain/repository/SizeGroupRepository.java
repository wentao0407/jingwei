package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.SizeGroup;

import java.util.List;

/**
 * 尺码组仓库接口
 * <p>
 * 提供尺码组的持久化操作，所有数据库操作通过此接口抽象。
 * </p>
 *
 * @author JingWei
 */
public interface SizeGroupRepository {

    /**
     * 根据ID查询尺码组
     *
     * @param id 尺码组ID
     * @return 尺码组实体，不存在返回 null
     */
    SizeGroup selectById(Long id);

    /**
     * 查询所有尺码组
     * <p>
     * 不含尺码详情，用于列表展示。
     * </p>
     *
     * @return 所有未删除的尺码组列表
     */
    List<SizeGroup> selectAll();

    /**
     * 根据品类筛选尺码组
     *
     * @param category 适用品类（WOMEN/MEN/CHILDREN），null 表示不限
     * @param status   状态（ACTIVE/INACTIVE），null 表示不限
     * @return 符合条件的尺码组列表
     */
    List<SizeGroup> selectByCondition(String category, String status);

    /**
     * 检查尺码组编码是否已存在
     *
     * @param code      尺码组编码
     * @param excludeId 排除的ID（更新时排除自身），可为 null
     * @return true=已存在
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 插入尺码组
     *
     * @param sizeGroup 尺码组实体
     * @return 影响行数
     */
    int insert(SizeGroup sizeGroup);

    /**
     * 更新尺码组
     *
     * @param sizeGroup 尺码组实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(SizeGroup sizeGroup);

    /**
     * 逻辑删除尺码组
     *
     * @param id 尺码组ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
