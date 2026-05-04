package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.procurement.domain.model.BomItem;
import com.jingwei.procurement.domain.repository.BomItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BOM 行项目仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BomItemRepositoryImpl implements BomItemRepository {

    private final BomItemMapper bomItemMapper;

    @Override
    public BomItem selectById(Long id) {
        return bomItemMapper.selectById(id);
    }

    @Override
    public List<BomItem> selectByBomId(Long bomId) {
        return bomItemMapper.selectList(
                new LambdaQueryWrapper<BomItem>()
                        .eq(BomItem::getBomId, bomId)
                        .orderByAsc(BomItem::getSortOrder));
    }

    @Override
    public int insert(BomItem bomItem) {
        return bomItemMapper.insert(bomItem);
    }

    @Override
    public int updateById(BomItem bomItem) {
        return bomItemMapper.updateById(bomItem);
    }

    @Override
    public int deleteById(Long id) {
        return bomItemMapper.deleteById(id);
    }

    @Override
    public int deleteByBomId(Long bomId) {
        return bomItemMapper.delete(
                new LambdaQueryWrapper<BomItem>()
                        .eq(BomItem::getBomId, bomId));
    }
}
