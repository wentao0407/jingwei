package com.jingwei.approval.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.approval.domain.model.ApprovalTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 审批任务 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface ApprovalTaskMapper extends BaseMapper<ApprovalTask> {

    /**
     * 取消同一业务的其他待办任务
     *
     * @param businessType  业务类型
     * @param businessId    业务单据ID
     * @param excludeTaskId 排除的任务ID
     * @return 取消的任务数量
     */
    @Update("UPDATE t_sys_approval_task SET status = 'CANCELLED', updated_at = NOW() " +
            "WHERE business_type = #{businessType} AND business_id = #{businessId} " +
            "AND status = 'PENDING' AND id != #{excludeTaskId} AND deleted = FALSE")
    int cancelOtherPendingTasks(@Param("businessType") String businessType,
                                @Param("businessId") Long businessId,
                                @Param("excludeTaskId") Long excludeTaskId);
}
