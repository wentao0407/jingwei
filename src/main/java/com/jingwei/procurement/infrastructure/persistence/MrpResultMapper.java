package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.procurement.domain.model.MrpResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * MRP 计算结果 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface MrpResultMapper extends BaseMapper<MrpResult> {
}
