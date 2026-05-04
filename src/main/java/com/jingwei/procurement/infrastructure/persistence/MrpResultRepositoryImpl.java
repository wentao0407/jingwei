package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.MrpResult;
import com.jingwei.procurement.domain.model.MrpResultStatus;
import com.jingwei.procurement.domain.repository.MrpResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * MRP 计算结果仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MrpResultRepositoryImpl implements MrpResultRepository {

    private final MrpResultMapper mrpResultMapper;

    @Override
    public MrpResult selectById(Long id) {
        return mrpResultMapper.selectById(id);
    }

    @Override
    public IPage<MrpResult> selectPage(IPage<MrpResult> page, String batchNo, MrpResultStatus status) {
        LambdaQueryWrapper<MrpResult> wrapper = new LambdaQueryWrapper<MrpResult>()
                .eq(batchNo != null && !batchNo.isEmpty(), MrpResult::getBatchNo, batchNo)
                .eq(status != null, MrpResult::getStatus, status)
                .orderByDesc(MrpResult::getCreatedAt);
        return mrpResultMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(MrpResult result) {
        return mrpResultMapper.insert(result);
    }

    @Override
    public int updateById(MrpResult result) {
        return mrpResultMapper.updateById(result);
    }

    @Override
    public int expirePendingByBatchNo(String batchNo) {
        return mrpResultMapper.update(null,
                new LambdaUpdateWrapper<MrpResult>()
                        .eq(MrpResult::getBatchNo, batchNo)
                        .eq(MrpResult::getStatus, MrpResultStatus.PENDING)
                        .set(MrpResult::getStatus, MrpResultStatus.EXPIRED));
    }
}
