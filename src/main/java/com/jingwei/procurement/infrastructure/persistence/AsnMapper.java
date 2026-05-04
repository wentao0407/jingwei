package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.Asn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 到货通知单 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface AsnMapper extends BaseMapper<Asn> {
}
