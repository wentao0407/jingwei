package com.jingwei.warehouse.domain.repository;

import com.jingwei.warehouse.domain.model.PickList;

import java.util.List;

/**
 * 拣货单仓库接口
 *
 * @author JingWei
 */
public interface PickListRepository {
    PickList selectById(Long id);
    List<PickList> selectByWaveId(Long waveId);
    int insert(PickList pickList);
    int updateById(PickList pickList);
}
