package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.warehouse.domain.model.MaterialIssueLine;
import com.jingwei.warehouse.domain.model.MaterialIssueOrder;
import com.jingwei.warehouse.domain.model.MaterialIssueStatus;
import com.jingwei.warehouse.domain.repository.MaterialIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MaterialIssueRepositoryImpl implements MaterialIssueRepository {

    private final MaterialIssueOrderMapper orderMapper;
    private final MaterialIssueLineMapper lineMapper;

    @Override
    public MaterialIssueOrder selectById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public MaterialIssueOrder selectDetailById(Long id) {
        MaterialIssueOrder order = orderMapper.selectById(id);
        if (order != null) {
            List<MaterialIssueLine> lines = lineMapper.selectList(
                    new LambdaQueryWrapper<MaterialIssueLine>().eq(MaterialIssueLine::getIssueId, id));
            order.setLines(lines);
        }
        return order;
    }

    @Override
    public int insert(MaterialIssueOrder order) {
        return orderMapper.insert(order);
    }

    @Override
    public int updateById(MaterialIssueOrder order) {
        return orderMapper.updateById(order);
    }

    @Override
    public IPage<MaterialIssueOrder> selectPage(Page<MaterialIssueOrder> page, MaterialIssueStatus status) {
        LambdaQueryWrapper<MaterialIssueOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(MaterialIssueOrder::getStatus, status);
        wrapper.orderByDesc(MaterialIssueOrder::getCreatedAt);
        return orderMapper.selectPage(page, wrapper);
    }
}
