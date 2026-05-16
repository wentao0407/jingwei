package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.warehouse.domain.model.MaterialReturnLine;
import com.jingwei.warehouse.domain.model.MaterialReturnOrder;
import com.jingwei.warehouse.domain.model.MaterialReturnStatus;
import com.jingwei.warehouse.domain.repository.MaterialReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MaterialReturnRepositoryImpl implements MaterialReturnRepository {

    private final MaterialReturnOrderMapper orderMapper;
    private final MaterialReturnLineMapper lineMapper;

    @Override
    public MaterialReturnOrder selectById(Long id) { return orderMapper.selectById(id); }

    @Override
    public MaterialReturnOrder selectDetailById(Long id) {
        MaterialReturnOrder order = orderMapper.selectById(id);
        if (order != null) {
            List<MaterialReturnLine> lines = lineMapper.selectList(
                    new LambdaQueryWrapper<MaterialReturnLine>().eq(MaterialReturnLine::getReturnId, id));
            order.setLines(lines);
        }
        return order;
    }

    @Override
    public int insert(MaterialReturnOrder order) { return orderMapper.insert(order); }

    @Override
    public int updateById(MaterialReturnOrder order) { return orderMapper.updateById(order); }

    @Override
    public IPage<MaterialReturnOrder> selectPage(Page<MaterialReturnOrder> page, MaterialReturnStatus status) {
        LambdaQueryWrapper<MaterialReturnOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(MaterialReturnOrder::getStatus, status);
        wrapper.orderByDesc(MaterialReturnOrder::getCreatedAt);
        return orderMapper.selectPage(page, wrapper);
    }
}
