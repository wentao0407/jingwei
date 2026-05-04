package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.ProcurementOrderLine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单行 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ProcurementOrderLineMapper extends BaseMapper<ProcurementOrderLine> {
}
