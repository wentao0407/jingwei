package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.model.SpuStatus;
import com.jingwei.master.domain.repository.SpuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SPU 仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SpuRepositoryImpl implements SpuRepository {

    private final SpuMapper spuMapper;

    @Override
    public Spu selectById(Long id) {
        return spuMapper.selectById(id);
    }

    @Override
    public List<Spu> selectAll() {
        return spuMapper.selectList(
                new LambdaQueryWrapper<Spu>()
                        .orderByDesc(Spu::getCreatedAt));
    }

    @Override
    public List<Spu> selectByCondition(String status, Long seasonId, Long categoryId) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Spu::getStatus, SpuStatus.valueOf(status));
        }
        if (seasonId != null) {
            wrapper.eq(Spu::getSeasonId, seasonId);
        }
        if (categoryId != null) {
            wrapper.eq(Spu::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Spu::getCreatedAt);
        return spuMapper.selectList(wrapper);
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<Spu>()
                .eq(Spu::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Spu::getId, excludeId);
        }
        return spuMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int insert(Spu spu) {
        return spuMapper.insert(spu);
    }

    @Override
    public int updateById(Spu spu) {
        return spuMapper.updateById(spu);
    }

    @Override
    public int deleteById(Long id) {
        return spuMapper.deleteById(id);
    }
}
