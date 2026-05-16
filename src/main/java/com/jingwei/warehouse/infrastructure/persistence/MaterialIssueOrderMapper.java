package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.warehouse.domain.model.MaterialIssueOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaterialIssueOrderMapper extends BaseMapper<MaterialIssueOrder> {
}
