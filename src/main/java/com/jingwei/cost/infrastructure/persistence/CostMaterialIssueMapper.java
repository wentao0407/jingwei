package com.jingwei.cost.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.cost.domain.model.CostMaterialIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 领料成本记录 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface CostMaterialIssueMapper extends BaseMapper<CostMaterialIssue> {

    /**
     * 查询某生产订单行的领料成本合计
     *
     * @param productionOrderId 生产订单ID
     * @param productionLineId  生产订单行ID
     * @return 领料成本合计
     */
    @Select("SELECT COALESCE(SUM(cost_amount), 0) FROM t_cost_material_issue " +
            "WHERE production_order_id = #{productionOrderId} " +
            "AND production_line_id = #{productionLineId} AND deleted = FALSE")
    BigDecimal sumCostByOrderLineId(@Param("productionOrderId") Long productionOrderId,
                                     @Param("productionLineId") Long productionLineId);
}
