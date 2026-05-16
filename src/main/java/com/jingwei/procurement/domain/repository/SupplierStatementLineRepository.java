package com.jingwei.procurement.domain.repository;

import com.jingwei.procurement.domain.model.SupplierStatementLine;

import java.util.List;

/**
 * 供应商对账单行仓库接口
 *
 * @author JingWei
 */
public interface SupplierStatementLineRepository {

    List<SupplierStatementLine> selectByStatementId(Long statementId);

    int insert(SupplierStatementLine line);

    int deleteByStatementId(Long statementId);
}
