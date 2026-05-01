package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Size;

import java.util.List;

/**
 * 尺码仓库接口
 * <p>
 * 提供尺码的持久化操作，所有数据库操作通过此接口抽象。
 * 尺码从属于尺码组，不支持独立存在。
 * </p>
 *
 * @author JingWei
 */
public interface SizeRepository {

    /**
     * 根据ID查询尺码
     *
     * @param id 尺码ID
     * @return 尺码实体，不存在返回 null
     */
    Size selectById(Long id);

    /**
     * 根据尺码组ID查询所有尺码
     *
     * @param sizeGroupId 尺码组ID
     * @return 该组下的所有未删除尺码，按 sort_order 排序
     */
    List<Size> selectBySizeGroupId(Long sizeGroupId);

    /**
     * 检查同一尺码组内编码是否已存在
     *
     * @param sizeGroupId 尺码组ID
     * @param code        尺码编码
     * @param excludeId   排除的ID（更新时排除自身），可为 null
     * @return true=已存在
     */
    boolean existsBySizeGroupIdAndCode(Long sizeGroupId, String code, Long excludeId);

    /**
     * 获取尺码组内的最大排序号
     * <p>
     * 新增尺码时追加到末尾，需要知道当前最大排序号。
     * </p>
     *
     * @param sizeGroupId 尺码组ID
     * @return 最大排序号，无尺码时返回 0
     */
    int getMaxSortOrder(Long sizeGroupId);

    /**
     * 插入尺码
     *
     * @param size 尺码实体
     * @return 影响行数
     */
    int insert(Size size);

    /**
     * 更新尺码
     *
     * @param size 尺码实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(Size size);

    /**
     * 逻辑删除尺码
     *
     * @param id 尺码ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 统计尺码组内的尺码数量
     *
     * @param sizeGroupId 尺码组ID
     * @return 尺码数量
     */
    long countBySizeGroupId(Long sizeGroupId);
}
