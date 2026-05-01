package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Wave;
import org.apache.ibatis.annotations.Mapper;

/**
 * 波段 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface WaveMapper extends BaseMapper<Wave> {
}
