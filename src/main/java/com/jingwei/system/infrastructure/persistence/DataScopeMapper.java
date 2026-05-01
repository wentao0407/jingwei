package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.system.domain.model.DataScope;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataScopeMapper extends BaseMapper<DataScope> {
}
