package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.StatementStatus;
import com.jingwei.procurement.domain.model.SupplierStatement;
import com.jingwei.procurement.domain.model.SupplierStatementLine;
import com.jingwei.procurement.domain.repository.SupplierStatementLineRepository;
import com.jingwei.procurement.domain.repository.SupplierStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 供应商对账单仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SupplierStatementRepositoryImpl implements SupplierStatementRepository {

    private final SupplierStatementMapper statementMapper;
    private final SupplierStatementLineRepository statementLineRepository;

    @Override
    public SupplierStatement selectById(Long id) {
        return statementMapper.selectById(id);
    }

    @Override
    public SupplierStatement selectDetailById(Long id) {
        SupplierStatement statement = statementMapper.selectById(id);
        if (statement != null) {
            List<SupplierStatementLine> lines = statementLineRepository.selectByStatementId(id);
            statement.setLines(lines);
        }
        return statement;
    }

    @Override
    public IPage<SupplierStatement> selectPage(IPage<SupplierStatement> page, Long supplierId, StatementStatus status) {
        LambdaQueryWrapper<SupplierStatement> wrapper = new LambdaQueryWrapper<SupplierStatement>()
                .eq(supplierId != null, SupplierStatement::getSupplierId, supplierId)
                .eq(status != null, SupplierStatement::getStatus, status)
                .orderByDesc(SupplierStatement::getCreatedAt);
        return statementMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(SupplierStatement statement) {
        return statementMapper.insert(statement);
    }

    @Override
    public int updateById(SupplierStatement statement) {
        return statementMapper.updateById(statement);
    }
}
