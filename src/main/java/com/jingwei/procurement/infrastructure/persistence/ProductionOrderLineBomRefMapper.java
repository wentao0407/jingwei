package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.ProductionOrderLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 生产订单行 Mapper（用于检查 BOM 引用关系）
 *
 * @author JingWei
 */
@Mapper
public interface ProductionOrderLineBomRefMapper extends BaseMapper<ProductionOrderLine> {

    /**
     * 检查指定 BOM 是否被生产订单行引用
     */
    @Select("SELECT EXISTS(SELECT 1 FROM t_order_production_line WHERE bom_id = #{bomId} AND deleted = FALSE)")
    boolean existsByBomId(@Param("bomId") Long bomId);
}
