package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.Bom;
import com.jingwei.procurement.domain.model.BomItem;
import com.jingwei.procurement.domain.model.BomStatus;
import com.jingwei.procurement.domain.repository.BomItemRepository;
import com.jingwei.procurement.domain.repository.BomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BOM 仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BomRepositoryImpl implements BomRepository {

    private final BomMapper bomMapper;
    private final BomItemRepository bomItemRepository;
    private final ProductionOrderLineBomRefMapper productionOrderLineBomRefMapper;

    @Override
    public Bom selectById(Long id) {
        return bomMapper.selectById(id);
    }

    @Override
    public Bom selectDetailById(Long id) {
        Bom bom = bomMapper.selectById(id);
        if (bom != null) {
            List<BomItem> items = bomItemRepository.selectByBomId(id);
            bom.setItems(items);
        }
        return bom;
    }

    @Override
    public IPage<Bom> selectPage(IPage<Bom> page, Long spuId, BomStatus status) {
        LambdaQueryWrapper<Bom> wrapper = new LambdaQueryWrapper<Bom>()
                .eq(spuId != null, Bom::getSpuId, spuId)
                .eq(status != null, Bom::getStatus, status)
                .orderByDesc(Bom::getSpuId)
                .orderByDesc(Bom::getBomVersion);
        return bomMapper.selectPage(page, wrapper);
    }

    @Override
    public Integer selectMaxBomVersion(Long spuId) {
        Bom result = bomMapper.selectOne(
                new LambdaQueryWrapper<Bom>()
                        .select(Bom::getBomVersion)
                        .eq(Bom::getSpuId, spuId)
                        .orderByDesc(Bom::getBomVersion)
                        .last("LIMIT 1"));
        return result != null ? result.getBomVersion() : null;
    }

    @Override
    public Optional<Bom> selectApprovedBySpuId(Long spuId) {
        Bom bom = bomMapper.selectOne(
                new LambdaQueryWrapper<Bom>()
                        .eq(Bom::getSpuId, spuId)
                        .eq(Bom::getStatus, BomStatus.APPROVED));
        return Optional.ofNullable(bom);
    }

    @Override
    public Optional<Bom> selectApprovedDetailBySpuId(Long spuId) {
        Optional<Bom> bomOpt = selectApprovedBySpuId(spuId);
        bomOpt.ifPresent(bom -> {
            List<BomItem> items = bomItemRepository.selectByBomId(bom.getId());
            bom.setItems(items);
        });
        return bomOpt;
    }

    @Override
    public boolean isReferencedByProductionOrder(Long bomId) {
        return productionOrderLineBomRefMapper.existsByBomId(bomId);
    }

    @Override
    public int insert(Bom bom) {
        return bomMapper.insert(bom);
    }

    @Override
    public int updateById(Bom bom) {
        return bomMapper.updateById(bom);
    }

    @Override
    public int deleteById(Long id) {
        return bomMapper.deleteById(id);
    }
}
