package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.Bom;
import org.apache.ibatis.annotations.Mapper;

/**
 * BOM Mapper
 *
 * @author JingWei
 */
@Mapper
public interface BomMapper extends BaseMapper<Bom> {
}
