package com.jingwei.procurement.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.MrpResult;
import com.jingwei.procurement.domain.model.MrpResultStatus;

/**
 * MRP 计算结果仓库接口
 *
 * @author JingWei
 */
public interface MrpResultRepository {

    MrpResult selectById(Long id);

    IPage<MrpResult> selectPage(IPage<MrpResult> page, String batchNo, MrpResultStatus status);

    int insert(MrpResult result);

    int updateById(MrpResult result);

    /**
     * 将指定批次的 PENDING 结果标记为 EXPIRED
     */
    int expirePendingByBatchNo(String batchNo);
}
