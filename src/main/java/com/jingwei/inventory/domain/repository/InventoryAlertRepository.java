package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.AlertStatus;
import com.jingwei.inventory.domain.model.InventoryAlert;

import java.util.List;

/**
 * 预警记录仓库接口
 *
 * @author JingWei
 */
public interface InventoryAlertRepository {

    InventoryAlert selectById(Long id);

    /** 查询指定 SKU + 规则的活跃预警（去重用） */
    InventoryAlert selectActiveBySkuAndRule(Long skuId, Long ruleId);

    /** 查询所有预警（分页/筛选） */
    List<InventoryAlert> selectByStatus(AlertStatus status);

    int insert(InventoryAlert alert);

    int updateById(InventoryAlert alert);
}
