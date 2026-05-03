package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.SalesOrderLine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售订单行 Mapper
 * <p>
 * 基于 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。
 * 订单行的 size_matrix 字段使用 {@link com.jingwei.order.domain.model.SizeMatrixTypeHandler} 处理 JSONB。
 * </p>
 *
 * @author JingWei
 */
@Mapper
public interface SalesOrderLineMapper extends BaseMapper<SalesOrderLine> {
}
