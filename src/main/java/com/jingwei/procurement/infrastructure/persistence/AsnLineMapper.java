package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.AsnLine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 到货通知单行 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface AsnLineMapper extends BaseMapper<AsnLine> {
}
