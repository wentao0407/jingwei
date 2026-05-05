package com.jingwei.common.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.common.domain.model.DomainEventConsumeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事件消费日志 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface DomainEventConsumeLogMapper extends BaseMapper<DomainEventConsumeLog> {
}
