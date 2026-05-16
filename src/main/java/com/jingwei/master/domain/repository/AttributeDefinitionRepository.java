package com.jingwei.master.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.AttributeDefinition;

import java.util.List;

/**
 * 属性定义仓库接口
 *
 * @author JingWei
 */
public interface AttributeDefinitionRepository {

    AttributeDefinition selectById(Long id);

    IPage<AttributeDefinition> selectPage(IPage<AttributeDefinition> page, String materialType, String keyword);

    List<AttributeDefinition> selectByMaterialType(String materialType);

    int insert(AttributeDefinition definition);

    int updateById(AttributeDefinition definition);

    int deleteById(Long id);
}
