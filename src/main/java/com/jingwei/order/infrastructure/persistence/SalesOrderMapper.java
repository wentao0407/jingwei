package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.SalesOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售订单 Mapper
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。
 * </p>
 *
 * @author JingWei
 */
@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
}
