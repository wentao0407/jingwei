package com.jingwei.procurement.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.StatementStatus;
import com.jingwei.procurement.domain.model.SupplierStatement;

/**
 * 供应商对账单仓库接口
 *
 * @author JingWei
 */
public interface SupplierStatementRepository {

    SupplierStatement selectById(Long id);

    SupplierStatement selectDetailById(Long id);

    IPage<SupplierStatement> selectPage(IPage<SupplierStatement> page, Long supplierId, StatementStatus status);

    int insert(SupplierStatement statement);

    int updateById(SupplierStatement statement);
}
