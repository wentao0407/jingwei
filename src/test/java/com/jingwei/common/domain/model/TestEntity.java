package com.jingwei.common.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 测试实体类
 * <p>
 * 用于验证 BaseEntity 的审计字段自动填充、逻辑删除、乐观锁功能。
 * 对应数据库表 t_test_entity。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_test_entity")
public class TestEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 名称 */
    private String name;
}
