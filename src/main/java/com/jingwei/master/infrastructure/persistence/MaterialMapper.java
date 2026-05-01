package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Material;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料主数据 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {
}
