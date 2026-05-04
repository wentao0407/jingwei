package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.procurement.domain.model.AsnLine;
import com.jingwei.procurement.domain.repository.AsnLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 到货通知单行仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AsnLineRepositoryImpl implements AsnLineRepository {

    private final AsnLineMapper asnLineMapper;

    @Override
    public AsnLine selectById(Long id) {
        return asnLineMapper.selectById(id);
    }

    @Override
    public List<AsnLine> selectByAsnId(Long asnId) {
        return asnLineMapper.selectList(
                new LambdaQueryWrapper<AsnLine>()
                        .eq(AsnLine::getAsnId, asnId)
                        .orderByAsc(AsnLine::getId));
    }

    @Override
    public int insert(AsnLine line) {
        return asnLineMapper.insert(line);
    }

    @Override
    public int updateById(AsnLine line) {
        return asnLineMapper.updateById(line);
    }
}
