package com.jingwei.warehouse.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.warehouse.domain.model.MaterialReturnOrder;
import com.jingwei.warehouse.domain.model.MaterialReturnStatus;

public interface MaterialReturnRepository {
    MaterialReturnOrder selectById(Long id);
    MaterialReturnOrder selectDetailById(Long id);
    int insert(MaterialReturnOrder order);
    int updateById(MaterialReturnOrder order);
    IPage<MaterialReturnOrder> selectPage(Page<MaterialReturnOrder> page, MaterialReturnStatus status);
}
