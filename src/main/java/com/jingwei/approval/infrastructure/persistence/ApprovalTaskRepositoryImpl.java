package com.jingwei.approval.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.approval.domain.model.ApprovalTask;
import com.jingwei.approval.domain.model.ApprovalTaskStatus;
import com.jingwei.approval.domain.repository.ApprovalTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批任务仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ApprovalTaskRepositoryImpl implements ApprovalTaskRepository {

    private final ApprovalTaskMapper approvalTaskMapper;

    @Override
    public ApprovalTask selectById(Long id) {
        return approvalTaskMapper.selectById(id);
    }

    @Override
    public int insert(ApprovalTask task) {
        return approvalTaskMapper.insert(task);
    }

    @Override
    public int updateById(ApprovalTask task) {
        return approvalTaskMapper.updateById(task);
    }

    @Override
    public List<ApprovalTask> selectPendingByApproverId(Long approverId) {
        LambdaQueryWrapper<ApprovalTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTask::getApproverId, approverId)
                .eq(ApprovalTask::getStatus, ApprovalTaskStatus.PENDING)
                .orderByDesc(ApprovalTask::getCreatedAt);
        return approvalTaskMapper.selectList(wrapper);
    }

    @Override
    public List<ApprovalTask> selectByBusiness(String businessType, Long businessId) {
        LambdaQueryWrapper<ApprovalTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTask::getBusinessType, businessType)
                .eq(ApprovalTask::getBusinessId, businessId)
                .orderByDesc(ApprovalTask::getCreatedAt);
        return approvalTaskMapper.selectList(wrapper);
    }

    @Override
    public int cancelOtherPendingTasks(String businessType, Long businessId, Long excludeTaskId) {
        return approvalTaskMapper.cancelOtherPendingTasks(businessType, businessId, excludeTaskId);
    }
}
