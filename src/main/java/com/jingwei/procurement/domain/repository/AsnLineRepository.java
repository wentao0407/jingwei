package com.jingwei.procurement.domain.repository;

import com.jingwei.procurement.domain.model.AsnLine;

import java.util.List;

/**
 * 到货通知单行仓库接口
 *
 * @author JingWei
 */
public interface AsnLineRepository {

    AsnLine selectById(Long id);

    List<AsnLine> selectByAsnId(Long asnId);

    int insert(AsnLine line);

    int updateById(AsnLine line);
}
