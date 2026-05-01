package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Season;
import org.apache.ibatis.annotations.Mapper;

/**
 * 季节 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SeasonMapper extends BaseMapper<Season> {
}
