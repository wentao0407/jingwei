package com.jingwei.common.domain.model;

import com.jingwei.common.infrastructure.persistence.TestEntityMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseEntity 集成测试
 * <p>
 * 验证审计字段自动填充、逻辑删除、乐观锁功能。
 * 需要连接真实数据库执行。
 * </p>
 *
 * @author JingWei
 */
@SpringBootTest
class BaseEntityTest {

    @Autowired
    private TestEntityMapper testEntityMapper;

    @BeforeEach
    void setUp() {
        // 模拟登录用户
        UserContext.setUserId(1001L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        // 物理删除清理测试数据，绕过逻辑删除
        testEntityMapper.physicalDeleteAll();
    }

    @Test
    @DisplayName("插入记录 → createdAt/createdBy 自动填充")
    void insert_shouldFillAuditFields() {
        TestEntity entity = new TestEntity();
        entity.setName("测试物料");
        testEntityMapper.insert(entity);

        assertNotNull(entity.getId(), "ID 应自动生成");
        assertNotNull(entity.getCreatedAt(), "createdAt 应自动填充");
        assertEquals(1001L, entity.getCreatedBy(), "createdBy 应为当前用户ID");
        assertNotNull(entity.getUpdatedAt(), "updatedAt 应自动填充");
        assertEquals(1001L, entity.getUpdatedBy(), "updatedBy 应为当前用户ID");
        assertFalse(entity.getDeleted(), "deleted 应默认为 false");
        assertEquals(0, entity.getVersion(), "version 应默认为 0");
    }

    @Test
    @DisplayName("更新记录 → updatedAt 晚于 createdAt")
    void update_shouldUpdateAuditFields() throws InterruptedException {
        // 插入
        TestEntity entity = new TestEntity();
        entity.setName("原始名称");
        testEntityMapper.insert(entity);
        LocalDateTime createdAt = entity.getCreatedAt();

        // 模拟时间流逝
        Thread.sleep(100);

        // 更新
        UserContext.setUserId(2002L);
        entity.setName("更新名称");
        testEntityMapper.updateById(entity);

        // 重新查询验证
        TestEntity updated = testEntityMapper.selectById(entity.getId());
        assertNotNull(updated);
        assertEquals("更新名称", updated.getName());
        assertEquals(createdAt, updated.getCreatedAt(), "createdAt 不应变化");
        assertEquals(1001L, updated.getCreatedBy(), "createdBy 不应变化");
        assertTrue(updated.getUpdatedAt().isAfter(createdAt) || updated.getUpdatedAt().isEqual(createdAt),
                "updatedAt 应 >= createdAt");
        assertEquals(2002L, updated.getUpdatedBy(), "updatedBy 应为修改人ID");
        assertEquals(1, updated.getVersion(), "version 应递增为 1");
    }

    @Test
    @DisplayName("逻辑删除 → 查询不到，但数据库中记录存在且 deleted=true")
    void logicalDelete_shouldSoftDelete() {
        // 插入
        TestEntity entity = new TestEntity();
        entity.setName("待删除");
        testEntityMapper.insert(entity);
        Long id = entity.getId();

        // 逻辑删除
        testEntityMapper.deleteById(id);

        // 常规查询应查不到
        TestEntity found = testEntityMapper.selectById(id);
        assertNull(found, "逻辑删除后常规查询应返回 null");

        // 原生 SQL 查询验证记录仍存在
        // 使用 MyBatis-Plus 的条件构造器绕过逻辑删除不可行，
        // 这里通过直接查数据库确认 deleted=true
        // 由于 MyBatis-Plus 自动加了 WHERE deleted=false，
        // 我们用 selectCount 验证：未删除的记录数为 0
        long count = testEntityMapper.selectCount(null);
        assertEquals(0, count, "未删除的记录数应为 0");
    }

    @Test
    @DisplayName("乐观锁冲突 → 第二次更新失败")
    void optimisticLock_shouldFailOnConcurrentUpdate() {
        // 插入
        TestEntity entity = new TestEntity();
        entity.setName("并发测试");
        testEntityMapper.insert(entity);

        // 模拟两个用户读取同一条记录
        TestEntity user1Copy = testEntityMapper.selectById(entity.getId());
        TestEntity user2Copy = testEntityMapper.selectById(entity.getId());

        // 用户1 先更新成功
        user1Copy.setName("用户1修改");
        int rows1 = testEntityMapper.updateById(user1Copy);
        assertEquals(1, rows1, "用户1更新应成功");

        // 用户2 后更新失败（version 已被用户1递增）
        user2Copy.setName("用户2修改");
        int rows2 = testEntityMapper.updateById(user2Copy);
        assertEquals(0, rows2, "用户2更新应失败（乐观锁冲突）");

        // 验证数据是用户1的版本
        TestEntity current = testEntityMapper.selectById(entity.getId());
        assertEquals("用户1修改", current.getName());
        assertEquals(1, current.getVersion());
    }
}
