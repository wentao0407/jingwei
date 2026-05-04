package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.MrpSource;
import org.apache.ibatis.annotations.Mapper;

/**
 * MRP 计算来源 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface MrpSourceMapper extends BaseMapper<MrpSource> {
}
