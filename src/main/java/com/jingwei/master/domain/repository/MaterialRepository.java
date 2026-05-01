package com.jingwei.master.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.model.MaterialType;

/**
 * 物料仓库接口
 * <p>
 * 提供物料主数据的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface MaterialRepository {

    /**
     * 根据ID查询物料
     *
     * @param id 物料ID
     * @return 物料实体，不存在返回 null
     */
    Material selectById(Long id);

    /**
     * 分页查询物料
     *
     * @param page   分页参数
     * @param type   物料类型（可选筛选条件）
     * @param categoryId 物料分类ID（可选筛选条件）
     * @param status 状态（可选筛选条件）
     * @param keyword 关键词（搜索编码或名称，可选）
     * @return 分页结果
     */
    IPage<Material> selectPage(IPage<Material> page, MaterialType type,
                               Long categoryId, String status, String keyword);

    /**
     * 插入物料
     *
     * @param material 物料实体
     * @return 影响行数
     */
    int insert(Material material);

    /**
     * 更新物料
     *
     * @param material 物料实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(Material material);

    /**
     * 逻辑删除物料
     *
     * @param id 物料ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 检查物料编码是否已存在
     *
     * @param code 物料编码
     * @return true=已存在
     */
    boolean existsByCode(String code);
}
