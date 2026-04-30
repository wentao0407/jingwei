package com.jingwei.master.infrastructure.persistence;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.CodingSequence;
import com.jingwei.master.domain.repository.CodingSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 编码流水号仓库实现
 * <p>
 * 核心方法 incrementAndGet 采用 INSERT ON CONFLICT DO NOTHING + SELECT FOR UPDATE 三步法，
 * 解决首次并发生成时 SELECT FOR UPDATE 锁不住"幽灵行"的竞态问题：
 * <ol>
 *   <li>INSERT ... ON CONFLICT DO NOTHING — 确保行一定存在，首个线程插入成功，其余静默跳过</li>
 *   <li>SELECT ... FOR UPDATE — 行已存在，可加行级锁</li>
 *   <li>UPDATE — 在锁保护下递增</li>
 * </ol>
 * 不使用"先 SELECT FOR UPDATE，为空再 INSERT"的方式，因为 PostgreSQL 中 FOR UPDATE
 * 锁不住不存在的行，多线程同时查到 null 后会同时 INSERT 导致唯一键冲突，
 * 且 PostgreSQL 事务内一旦出错即标记为 aborted，后续所有 SQL 都会失败。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CodingSequenceRepositoryImpl implements CodingSequenceRepository {

    private final CodingSequenceMapper codingSequenceMapper;

    @Override
    public long incrementAndGet(Long ruleId, String resetKey, int seqLength) {
        // 1. 确保流水号行存在（ON CONFLICT DO NOTHING 保证并发安全）
        //    首次生成时多线程可能同时执行此 INSERT，但只有一个会成功插入，其余跳过
        //    初始 current_value = 0，不作为有效值，后续步骤统一递增
        long id = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
        codingSequenceMapper.insertOnConflictDoNothing(id, ruleId, resetKey);

        // 2. 加行级锁查询（此时行一定存在）
        CodingSequence seq = codingSequenceMapper.selectForUpdate(ruleId, resetKey);

        // 3. 计算上限：seqLength 位数的最大值（如 4 位上限 9999）
        long maxValue = (long) Math.pow(10, seqLength) - 1;
        long nextValue = seq.getCurrentValue() + 1;
        if (nextValue > maxValue) {
            throw new BizException(ErrorCode.CODING_SEQUENCE_EXHAUSTED);
        }

        // 4. 递增并更新
        codingSequenceMapper.updateCurrentValue(seq.getId(), nextValue);

        log.debug("递增流水号: ruleId={}, resetKey={}, value={}", ruleId, resetKey, nextValue);
        return nextValue;
    }
}
