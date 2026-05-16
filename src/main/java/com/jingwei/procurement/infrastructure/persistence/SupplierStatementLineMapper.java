package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.SupplierStatementLine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商对账单行 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SupplierStatementLineMapper extends BaseMapper<SupplierStatementLine> {
}
