package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 编码流水号实体
 * <p>
 * 对应数据库表 t_md_coding_sequence，记录每个规则在当前重置键下的流水号值。
 * 通过 SELECT ... FOR UPDATE 行级锁保证原子递增，避免并发时生成重复编号。
 * </p>
 * <p>
 * 不继承 BaseEntity（无审计字段和乐观锁），因为此表只做原子递增操作，
 * 不走 MyBatis-Plus 的逻辑删除和乐观锁机制。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_coding_sequence")
public class CodingSequence {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 规则ID */
    private Long ruleId;

    /** 重置键（如 202604 表示按月重置时的当前月份，NEVER 时为空字符串） */
    private String resetKey;

    /** 当前流水号值 */
    private Long currentValue;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
