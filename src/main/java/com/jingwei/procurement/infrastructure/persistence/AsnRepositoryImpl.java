package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.Asn;
import com.jingwei.procurement.domain.model.AsnLine;
import com.jingwei.procurement.domain.model.AsnStatus;
import com.jingwei.procurement.domain.repository.AsnLineRepository;
import com.jingwei.procurement.domain.repository.AsnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 到货通知单仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AsnRepositoryImpl implements AsnRepository {

    private final AsnMapper asnMapper;
    private final AsnLineRepository asnLineRepository;

    @Override
    public Asn selectById(Long id) {
        return asnMapper.selectById(id);
    }

    @Override
    public Asn selectDetailById(Long id) {
        Asn asn = asnMapper.selectById(id);
        if (asn != null) {
            List<AsnLine> lines = asnLineRepository.selectByAsnId(id);
            asn.setLines(lines);
        }
        return asn;
    }

    @Override
    public IPage<Asn> selectPage(IPage<Asn> page, Long procurementOrderId, AsnStatus status) {
        LambdaQueryWrapper<Asn> wrapper = new LambdaQueryWrapper<Asn>()
                .eq(procurementOrderId != null, Asn::getProcurementOrderId, procurementOrderId)
                .eq(status != null, Asn::getStatus, status)
                .orderByDesc(Asn::getCreatedAt);
        return asnMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(Asn asn) {
        return asnMapper.insert(asn);
    }

    @Override
    public int updateById(Asn asn) {
        return asnMapper.updateById(asn);
    }
}
