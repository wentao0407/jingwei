package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.AttributeDef;
import com.jingwei.master.domain.model.MaterialType;

import java.util.List;

/**
 * 属性定义仓库接口
 * <p>
 * 提供属性定义的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface AttributeDefRepository {

    /**
     * 根据物料类型查询属性定义列表
     *
     * @param materialType 物料类型
     * @return 属性定义列表，按 sort_order 排序
     */
    List<AttributeDef> selectByMaterialType(MaterialType materialType);

    /**
     * 查询所有属性定义
     *
     * @return 所有属性定义列表
     */
    List<AttributeDef> selectAll();

    /**
     * 根据ID查询属性定义
     *
     * @param id 属性定义ID
     * @return 属性定义实体
     */
    AttributeDef selectById(Long id);

    /**
     * 插入属性定义
     *
     * @param attributeDef 属性定义实体
     * @return 影响行数
     */
    int insert(AttributeDef attributeDef);

    /**
     * 更新属性定义
     *
     * @param attributeDef 属性定义实体
     * @return 影响行数
     */
    int updateById(AttributeDef attributeDef);
}
