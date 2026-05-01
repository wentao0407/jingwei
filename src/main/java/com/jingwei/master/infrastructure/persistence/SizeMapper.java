package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Size;
import org.apache.ibatis.annotations.Mapper;

/**
 * 尺码 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SizeMapper extends BaseMapper<Size> {
}
