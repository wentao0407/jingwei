package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.warehouse.domain.model.PickList;
import com.jingwei.warehouse.domain.repository.PickItemRepository;
import com.jingwei.warehouse.domain.repository.PickListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PickListRepositoryImpl implements PickListRepository {

    private final PickListMapper pickListMapper;
    private final PickItemRepository pickItemRepository;

    @Override
    public PickList selectById(Long id) {
        return pickListMapper.selectById(id);
    }

    @Override
    public List<PickList> selectByWaveId(Long waveId) {
        List<PickList> lists = pickListMapper.selectList(
                new LambdaQueryWrapper<PickList>()
                        .eq(PickList::getWaveId, waveId));
        for (PickList pl : lists) {
            pl.setItems(pickItemRepository.selectByPickListId(pl.getId()));
        }
        return lists;
    }

    @Override
    public int insert(PickList pickList) {
        return pickListMapper.insert(pickList);
    }

    @Override
    public int updateById(PickList pickList) {
        return pickListMapper.updateById(pickList);
    }
}
