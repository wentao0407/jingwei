package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.SupplierStatement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商对账单 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SupplierStatementMapper extends BaseMapper<SupplierStatement> {
}
