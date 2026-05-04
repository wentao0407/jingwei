package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.procurement.domain.model.MrpSource;
import com.jingwei.procurement.domain.repository.MrpSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MRP 计算来源仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MrpSourceRepositoryImpl implements MrpSourceRepository {

    private final MrpSourceMapper mrpSourceMapper;

    @Override
    public List<MrpSource> selectByResultId(Long resultId) {
        return mrpSourceMapper.selectList(
                new LambdaQueryWrapper<MrpSource>()
                        .eq(MrpSource::getResultId, resultId));
    }

    @Override
    public List<MrpSource> selectByBatchNo(String batchNo) {
        return mrpSourceMapper.selectList(
                new LambdaQueryWrapper<MrpSource>()
                        .eq(MrpSource::getBatchNo, batchNo));
    }

    @Override
    public int insert(MrpSource source) {
        return mrpSourceMapper.insert(source);
    }

    @Override
    public int batchInsert(List<MrpSource> sources) {
        int count = 0;
        for (MrpSource source : sources) {
            count += mrpSourceMapper.insert(source);
        }
        return count;
    }
}
