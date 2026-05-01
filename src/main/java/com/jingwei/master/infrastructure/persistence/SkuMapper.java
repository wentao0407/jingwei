package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * SKU Mapper
 *
 * @author JingWei
 */
@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
}
