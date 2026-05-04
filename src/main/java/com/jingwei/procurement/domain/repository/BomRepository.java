package com.jingwei.procurement.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.Bom;
import com.jingwei.procurement.domain.model.BomStatus;

import java.util.Optional;

/**
 * BOM 仓库接口
 *
 * @author JingWei
 */
public interface BomRepository {

    Bom selectById(Long id);

    Bom selectDetailById(Long id);

    IPage<Bom> selectPage(IPage<Bom> page, Long spuId, BomStatus status);

    /**
     * 查询指定SPU的最大版本号
     */
    Integer selectMaxBomVersion(Long spuId);

    /**
     * 查询指定SPU的已审批BOM
     */
    Optional<Bom> selectApprovedBySpuId(Long spuId);

    /**
     * 查询指定SPU的已审批BOM（含行）
     */
    Optional<Bom> selectApprovedDetailBySpuId(Long spuId);

    /**
     * 检查BOM是否被生产订单引用
     */
    boolean isReferencedByProductionOrder(Long bomId);

    int insert(Bom bom);

    int updateById(Bom bom);

    int deleteById(Long id);
}
