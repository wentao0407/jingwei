package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.SizeGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 尺码组 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SizeGroupMapper extends BaseMapper<SizeGroup> {
}
