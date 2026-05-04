package com.jingwei.procurement.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.Asn;
import com.jingwei.procurement.domain.model.AsnStatus;

/**
 * 到货通知单仓库接口
 *
 * @author JingWei
 */
public interface AsnRepository {

    Asn selectById(Long id);

    Asn selectDetailById(Long id);

    IPage<Asn> selectPage(IPage<Asn> page, Long procurementOrderId, AsnStatus status);

    int insert(Asn asn);

    int updateById(Asn asn);
}
