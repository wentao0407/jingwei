package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.ColorWay;
import org.apache.ibatis.annotations.Mapper;

/**
 * 颜色款 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ColorWayMapper extends BaseMapper<ColorWay> {
}
