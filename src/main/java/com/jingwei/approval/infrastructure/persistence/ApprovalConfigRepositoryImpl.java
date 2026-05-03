package com.jingwei.approval.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.approval.domain.model.ApprovalConfig;
import com.jingwei.approval.domain.repository.ApprovalConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批配置仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ApprovalConfigRepositoryImpl implements ApprovalConfigRepository {

    private final ApprovalConfigMapper approvalConfigMapper;

    @Override
    public ApprovalConfig selectById(Long id) {
        return approvalConfigMapper.selectById(id);
    }

    @Override
    public ApprovalConfig selectByBusinessType(String businessType) {
        LambdaQueryWrapper<ApprovalConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalConfig::getBusinessType, businessType);
        return approvalConfigMapper.selectOne(wrapper);
    }

    @Override
    public List<ApprovalConfig> selectAll() {
        LambdaQueryWrapper<ApprovalConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ApprovalConfig::getBusinessType);
        return approvalConfigMapper.selectList(wrapper);
    }

    @Override
    public int insert(ApprovalConfig config) {
        return approvalConfigMapper.insert(config);
    }

    @Override
    public int updateById(ApprovalConfig config) {
        return approvalConfigMapper.updateById(config);
    }

    @Override
    public int deleteById(Long id) {
        return approvalConfigMapper.deleteById(id);
    }

    @Override
    public boolean existsByBusinessType(String businessType) {
        LambdaQueryWrapper<ApprovalConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalConfig::getBusinessType, businessType);
        return approvalConfigMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByBusinessTypeExcludeId(String businessType, Long excludeId) {
        LambdaQueryWrapper<ApprovalConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalConfig::getBusinessType, businessType)
                .ne(ApprovalConfig::getId, excludeId);
        return approvalConfigMapper.selectCount(wrapper) > 0;
    }
}
