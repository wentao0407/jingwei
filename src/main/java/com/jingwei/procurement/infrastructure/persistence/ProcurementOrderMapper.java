package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.ProcurementOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ProcurementOrderMapper extends BaseMapper<ProcurementOrder> {
}
