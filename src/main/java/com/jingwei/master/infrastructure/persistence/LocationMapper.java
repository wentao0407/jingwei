package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Location;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库位 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface LocationMapper extends BaseMapper<Location> {
}
