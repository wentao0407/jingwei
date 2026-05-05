package com.jingwei.warehouse.domain.repository;

import com.jingwei.warehouse.domain.model.ReceivingLine;

import java.util.List;

/**
 * 收货行仓库接口
 *
 * @author JingWei
 */
public interface ReceivingLineRepository {

    ReceivingLine selectById(Long id);

    List<ReceivingLine> selectByReceivingId(Long receivingId);

    int batchInsert(List<ReceivingLine> lines);

    int updateById(ReceivingLine line);
}
