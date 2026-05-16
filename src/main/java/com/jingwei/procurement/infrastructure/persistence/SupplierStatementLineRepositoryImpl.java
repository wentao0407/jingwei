package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.procurement.domain.model.SupplierStatementLine;
import com.jingwei.procurement.domain.repository.SupplierStatementLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 供应商对账单行仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SupplierStatementLineRepositoryImpl implements SupplierStatementLineRepository {

    private final SupplierStatementLineMapper lineMapper;

    @Override
    public List<SupplierStatementLine> selectByStatementId(Long statementId) {
        return lineMapper.selectList(
                new LambdaQueryWrapper<SupplierStatementLine>()
                        .eq(SupplierStatementLine::getStatementId, statementId)
                        .orderByAsc(SupplierStatementLine::getId));
    }

    @Override
    public int insert(SupplierStatementLine line) {
        return lineMapper.insert(line);
    }

    @Override
    public int deleteByStatementId(Long statementId) {
        return lineMapper.delete(
                new LambdaQueryWrapper<SupplierStatementLine>()
                        .eq(SupplierStatementLine::getStatementId, statementId));
    }
}
