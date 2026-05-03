package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.OrderChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单变更日志 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface OrderChangeLogMapper extends BaseMapper<OrderChangeLog> {
}
