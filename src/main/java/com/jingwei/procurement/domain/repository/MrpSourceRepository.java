package com.jingwei.procurement.domain.repository;

import com.jingwei.procurement.domain.model.MrpSource;

import java.util.List;

/**
 * MRP 计算来源仓库接口
 *
 * @author JingWei
 */
public interface MrpSourceRepository {

    List<MrpSource> selectByResultId(Long resultId);

    List<MrpSource> selectByBatchNo(String batchNo);

    int insert(MrpSource source);

    int batchInsert(List<MrpSource> sources);
}
