package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.AttributeDef;
import org.apache.ibatis.annotations.Mapper;

/**
 * 属性定义 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface AttributeDefMapper extends BaseMapper<AttributeDef> {
}
