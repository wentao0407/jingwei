package com.jingwei.approval.domain.repository;

import com.jingwei.approval.domain.model.ApprovalTask;

import java.util.List;

/**
 * 审批任务仓库接口
 * <p>
 * 定义审批任务数据的持久化操作，由 infrastructure 层实现。
 * </p>
 *
 * @author JingWei
 */
public interface ApprovalTaskRepository {

    /**
     * 根据ID查询审批任务
     *
     * @param id 主键
     * @return 审批任务实体，不存在返回 null
     */
    ApprovalTask selectById(Long id);

    /**
     * 新增审批任务
     *
     * @param task 审批任务实体
     * @return 影响行数
     */
    int insert(ApprovalTask task);

    /**
     * 更新审批任务
     *
     * @param task 审批任务实体
     * @return 影响行数
     */
    int updateById(ApprovalTask task);

    /**
     * 查询指定审批人的待办任务列表
     *
     * @param approverId 审批人用户ID
     * @return 待办任务列表
     */
    List<ApprovalTask> selectPendingByApproverId(Long approverId);

    /**
     * 查询指定业务单据的审批任务列表
     *
     * @param businessType 业务类型
     * @param businessId   业务单据ID
     * @return 审批任务列表
     */
    List<ApprovalTask> selectByBusiness(String businessType, Long businessId);

    /**
     * 取消同一业务的其他待办任务（或签模式使用）
     * <p>
     * 当或签模式下某一审批人审批后，其他待办任务需要自动取消。
     * </p>
     *
     * @param businessType 业务类型
     * @param businessId   业务单据ID
     * @param excludeTaskId 排除的任务ID（已审批的那条）
     * @return 取消的任务数量
     */
    int cancelOtherPendingTasks(String businessType, Long businessId, Long excludeTaskId);
}
