package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.AttributeDef;
import com.jingwei.master.domain.model.MaterialType;
import com.jingwei.master.domain.repository.AttributeDefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 属性定义仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class AttributeDefRepositoryImpl implements AttributeDefRepository {

    private final AttributeDefMapper attributeDefMapper;

    @Override
    public List<AttributeDef> selectByMaterialType(MaterialType materialType) {
        return attributeDefMapper.selectList(
                new LambdaQueryWrapper<AttributeDef>()
                        .eq(AttributeDef::getMaterialType, materialType)
                        .orderByAsc(AttributeDef::getSortOrder));
    }

    @Override
    public List<AttributeDef> selectAll() {
        return attributeDefMapper.selectList(
                new LambdaQueryWrapper<AttributeDef>()
                        .orderByAsc(AttributeDef::getMaterialType)
                        .orderByAsc(AttributeDef::getSortOrder));
    }

    @Override
    public AttributeDef selectById(Long id) {
        return attributeDefMapper.selectById(id);
    }

    @Override
    public int insert(AttributeDef attributeDef) {
        return attributeDefMapper.insert(attributeDef);
    }

    @Override
    public int updateById(AttributeDef attributeDef) {
        return attributeDefMapper.updateById(attributeDef);
    }
}
