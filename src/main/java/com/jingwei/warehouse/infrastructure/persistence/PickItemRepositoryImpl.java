package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.warehouse.domain.model.PickItem;
import com.jingwei.warehouse.domain.repository.PickItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PickItemRepositoryImpl implements PickItemRepository {

    private final PickItemMapper pickItemMapper;

    @Override
    public PickItem selectById(Long id) {
        return pickItemMapper.selectById(id);
    }

    @Override
    public List<PickItem> selectByPickListId(Long pickListId) {
        return pickItemMapper.selectList(
                new LambdaQueryWrapper<PickItem>()
                        .eq(PickItem::getPickListId, pickListId));
    }

    @Override
    public int batchInsert(List<PickItem> items) {
        int count = 0;
        for (PickItem item : items) {
            count += pickItemMapper.insert(item);
        }
        return count;
    }

    @Override
    public int updateById(PickItem item) {
        return pickItemMapper.updateById(item);
    }
}
