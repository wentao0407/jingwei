package com.jingwei.warehouse.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.warehouse.domain.model.MaterialIssueOrder;
import com.jingwei.warehouse.domain.model.MaterialIssueStatus;

public interface MaterialIssueRepository {
    MaterialIssueOrder selectById(Long id);
    MaterialIssueOrder selectDetailById(Long id);
    int insert(MaterialIssueOrder order);
    int updateById(MaterialIssueOrder order);
    IPage<MaterialIssueOrder> selectPage(Page<MaterialIssueOrder> page, MaterialIssueStatus status);
}
