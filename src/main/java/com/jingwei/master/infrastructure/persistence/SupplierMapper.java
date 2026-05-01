package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Supplier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {
}
