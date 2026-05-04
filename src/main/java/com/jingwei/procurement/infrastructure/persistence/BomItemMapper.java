package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.BomItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * BOM 行项目 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface BomItemMapper extends BaseMapper<BomItem> {
}
