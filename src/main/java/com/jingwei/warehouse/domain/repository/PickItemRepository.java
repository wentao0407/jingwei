package com.jingwei.warehouse.domain.repository;

import com.jingwei.warehouse.domain.model.PickItem;

import java.util.List;

/**
 * 拣货项仓库接口
 *
 * @author JingWei
 */
public interface PickItemRepository {
    PickItem selectById(Long id);
    List<PickItem> selectByPickListId(Long pickListId);
    int batchInsert(List<PickItem> items);
    int updateById(PickItem item);
}
