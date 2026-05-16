package com.jingwei.inventory.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.TransferOrder;
import com.jingwei.inventory.domain.model.TransferStatus;

/**
 * 调拨单仓库接口
 *
 * @author JingWei
 */
public interface TransferOrderRepository {

    TransferOrder selectById(Long id);

    TransferOrder selectDetailById(Long id);

    int insert(TransferOrder order);

    int updateById(TransferOrder order);

    IPage<TransferOrder> selectPage(Page<TransferOrder> page, TransferStatus status);
}
