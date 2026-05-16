package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.AttributeDefinition;
import com.jingwei.master.domain.repository.AttributeDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 属性定义仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AttributeDefinitionRepositoryImpl implements AttributeDefinitionRepository {

    private final AttributeDefinitionMapper mapper;

    @Override
    public AttributeDefinition selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public IPage<AttributeDefinition> selectPage(IPage<AttributeDefinition> page, String materialType, String keyword) {
        LambdaQueryWrapper<AttributeDefinition> wrapper = new LambdaQueryWrapper<AttributeDefinition>()
                .eq(materialType != null && !materialType.isEmpty(), AttributeDefinition::getMaterialType, materialType)
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(AttributeDefinition::getCode, keyword)
                        .or().like(AttributeDefinition::getName, keyword))
                .orderByAsc(AttributeDefinition::getSortOrder)
                .orderByAsc(AttributeDefinition::getId);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<AttributeDefinition> selectByMaterialType(String materialType) {
        return mapper.selectList(
                new LambdaQueryWrapper<AttributeDefinition>()
                        .eq(AttributeDefinition::getMaterialType, materialType)
                        .orderByAsc(AttributeDefinition::getSortOrder)
                        .orderByAsc(AttributeDefinition::getId));
    }

    @Override
    public int insert(AttributeDefinition definition) {
        return mapper.insert(definition);
    }

    @Override
    public int updateById(AttributeDefinition definition) {
        return mapper.updateById(definition);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }
}
