package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.ReturnOrderLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 退货单行 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ReturnOrderLineMapper extends BaseMapper<ReturnOrderLine> {

    /**
     * 查询某订单行的退货数量合计（排除已删除记录）
     *
     * @param salesOrderLineId 原销售订单行ID
     * @return 退货数量合计
     */
    @Select("SELECT COALESCE(SUM(total_quantity), 0) FROM t_order_return_line " +
            "WHERE sales_order_line_id = #{salesOrderLineId} AND deleted = FALSE")
    int sumReturnQtyBySalesOrderLineId(@Param("salesOrderLineId") Long salesOrderLineId);
}
