package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Spu;
import org.apache.ibatis.annotations.Mapper;

/**
 * SPU 款式 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SpuMapper extends BaseMapper<Spu> {
}
