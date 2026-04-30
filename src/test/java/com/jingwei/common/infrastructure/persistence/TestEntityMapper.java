package com.jingwei.common.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.common.domain.model.TestEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试实体 Mapper
 * <p>
 * 用于验证 BaseEntity 的审计字段、逻辑删除、乐观锁功能。
 * </p>
 *
 * @author JingWei
 */
@Mapper
public interface TestEntityMapper extends BaseMapper<TestEntity> {

    /**
     * 物理删除所有记录（仅用于测试清理，绕过逻辑删除）
     */
    @Delete("DELETE FROM t_test_entity")
    void physicalDeleteAll();
}
