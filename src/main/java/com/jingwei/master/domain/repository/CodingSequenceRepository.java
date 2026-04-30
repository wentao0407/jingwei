package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.CodingSequence;

/**
 * 编码流水号仓库接口
 * <p>
 * 核心：incrementAndGet 使用 SELECT ... FOR UPDATE 行级锁保证原子递增。
 * </p>
 *
 * @author JingWei
 */
public interface CodingSequenceRepository {

    /**
     * 原子递增并获取下一个流水号
     * <p>
     * 使用 INSERT ON CONFLICT DO NOTHING + SELECT FOR UPDATE 保证并发安全：
     * 1. INSERT ON CONFLICT DO NOTHING — 确保行存在，避免首次并发时的唯一键冲突
     * 2. SELECT FOR UPDATE — 加行级锁，保证原子递增
     * 3. UPDATE — 在锁保护下递增 current_value
     * </p>
     * <p>
     * 不使用"先 SELECT FOR UPDATE 再 INSERT"的方式，因为 FOR UPDATE 锁不住不存在的行，
     * 多线程首次生成时都会查到 null 然后同时 INSERT，在 PostgreSQL 下触发唯一键冲突
     * 且会将事务标记为 aborted 导致后续所有 SQL 失败。
     * </p>
     *
     * @param ruleId    规则ID
     * @param resetKey  重置键
     * @param seqLength 流水号位数（用于上限校验）
     * @return 递增后的流水号值
     */
    long incrementAndGet(Long ruleId, String resetKey, int seqLength);
}
