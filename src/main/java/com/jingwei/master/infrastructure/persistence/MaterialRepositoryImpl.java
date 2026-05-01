package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.model.MaterialType;
import com.jingwei.master.domain.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 物料仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 MaterialRepository 接口。
 * </p>
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class MaterialRepositoryImpl implements MaterialRepository {

    private final MaterialMapper materialMapper;

    @Override
    public Material selectById(Long id) {
        return materialMapper.selectById(id);
    }

    @Override
    public IPage<Material> selectPage(IPage<Material> page, MaterialType type,
                                       Long categoryId, String status, String keyword) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<Material>()
                .eq(type != null, Material::getType, type)
                .eq(categoryId != null, Material::getCategoryId, categoryId)
                .eq(status != null, Material::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), w ->
                        w.like(Material::getCode, keyword)
                                .or()
                                .like(Material::getName, keyword))
                .orderByDesc(Material::getCreatedAt);
        return materialMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(Material material) {
        return materialMapper.insert(material);
    }

    @Override
    public int updateById(Material material) {
        return materialMapper.updateById(material);
    }

    @Override
    public int deleteById(Long id) {
        return materialMapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return materialMapper.selectCount(
                new LambdaQueryWrapper<Material>()
                        .eq(Material::getCode, code)) > 0;
    }
}
