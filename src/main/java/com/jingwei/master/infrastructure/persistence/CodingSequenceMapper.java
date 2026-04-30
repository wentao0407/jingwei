package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.CodingSequence;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 编码流水号 Mapper
 * <p>
 * 核心方法使用 INSERT ... ON CONFLICT DO NOTHING + SELECT ... FOR UPDATE
 * 保证首次并发插入也能安全递增，避免唯一键冲突。
 * </p>
 *
 * @author JingWei
 */
@Mapper
public interface CodingSequenceMapper extends BaseMapper<CodingSequence> {

    /**
     * 插入初始流水号行，若 (rule_id, reset_key) 已存在则跳过
     * <p>
     * 使用 INSERT ... ON CONFLICT DO NOTHING 解决首次并发生成时的竞态问题：
     * 当多个线程同时为同一个 (ruleId, resetKey) 首次生成编码时，
     * SELECT ... FOR UPDATE 查不到行（行尚不存在，无法加锁），
     * 若直接 INSERT 则全部线程都会尝试插入，导致唯一键冲突。
     * ON CONFLICT DO NOTHING 保证只有一个线程插入成功，其余静默跳过，
     * 随后所有线程都能通过 SELECT ... FOR UPDATE 锁住已存在的行。
     * </p>
     *
     * @param ruleId   规则ID
     * @param resetKey 重置键
     * @return 实际插入的行数（1=首次创建，0=行已存在被跳过）
     */
    @Insert("INSERT INTO t_md_coding_sequence (id, rule_id, reset_key, current_value, created_at, updated_at) " +
            "VALUES (#{id}, #{ruleId}, #{resetKey}, 0, NOW(), NOW()) " +
            "ON CONFLICT (rule_id, reset_key) DO NOTHING")
    int insertOnConflictDoNothing(@Param("id") Long id, @Param("ruleId") Long ruleId, @Param("resetKey") String resetKey);

    /**
     * 加行级锁查询当前流水号
     * <p>
     * SELECT ... FOR UPDATE 保证并发安全：同一 (rule_id, reset_key) 只有一个事务可以获取到锁。
     * 调用前必须先执行 insertOnConflictDoNothing 确保行已存在，否则 FOR UPDATE 锁不住"幽灵行"。
     * </p>
     */
    @Select("SELECT * FROM t_md_coding_sequence WHERE rule_id = #{ruleId} AND reset_key = #{resetKey} FOR UPDATE")
    CodingSequence selectForUpdate(@Param("ruleId") Long ruleId, @Param("resetKey") String resetKey);

    /**
     * 更新流水号值
     */
    @Update("UPDATE t_md_coding_sequence SET current_value = #{currentValue}, updated_at = NOW() WHERE id = #{id}")
    int updateCurrentValue(@Param("id") Long id, @Param("currentValue") long currentValue);
}
