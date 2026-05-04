package com.jingwei.procurement.domain.repository;

import com.jingwei.procurement.domain.model.BomItem;

import java.util.List;

/**
 * BOM 行项目仓库接口
 *
 * @author JingWei
 */
public interface BomItemRepository {

    BomItem selectById(Long id);

    List<BomItem> selectByBomId(Long bomId);

    int insert(BomItem bomItem);

    int updateById(BomItem bomItem);

    int deleteById(Long id);

    int deleteByBomId(Long bomId);
}
