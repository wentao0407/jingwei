package com.jingwei.approval.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.approval.domain.model.ApprovalConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批配置 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface ApprovalConfigMapper extends BaseMapper<ApprovalConfig> {
}
