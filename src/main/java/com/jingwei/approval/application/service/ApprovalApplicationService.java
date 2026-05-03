package com.jingwei.approval.application.service;

import com.jingwei.approval.application.dto.ApproveDTO;
import com.jingwei.approval.application.dto.CreateApprovalConfigDTO;
import com.jingwei.approval.application.dto.SubmitApprovalDTO;
import com.jingwei.approval.application.dto.UpdateApprovalConfigDTO;
import com.jingwei.approval.domain.model.ApprovalConfig;
import com.jingwei.approval.domain.model.ApprovalMode;
import com.jingwei.approval.domain.model.ApprovalTask;
import com.jingwei.approval.domain.repository.ApprovalConfigRepository;
import com.jingwei.approval.domain.repository.ApprovalTaskRepository;
import com.jingwei.approval.domain.service.ApprovalDomainService;
import com.jingwei.approval.interfaces.vo.ApprovalConfigVO;
import com.jingwei.approval.interfaces.vo.ApprovalTaskVO;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.domain.model.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 审批应用服务（编排层）
 * <p>
 * 负责事务管理、DTO↔实体转换、调用领域服务。
 * 不包含业务逻辑，所有业务规则在 {@link ApprovalDomainService} 中。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalApplicationService {

    private final ApprovalDomainService domainService;
    private final ApprovalConfigRepository configRepository;
    private final ApprovalTaskRepository taskRepository;

    // ========== 审批配置 CRUD ==========

    /**
     * 创建审批配置
     */
    @Transactional
    public ApprovalConfigVO createConfig(CreateApprovalConfigDTO dto) {
        ApprovalConfig config = new ApprovalConfig();
        config.setBusinessType(dto.getBusinessType());
        config.setConfigName(dto.getConfigName());
        config.setApprovalMode(ApprovalMode.valueOf(dto.getApprovalMode()));
        config.setApproverRoleIds(dto.getApproverRoleIds());
        config.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        domainService.validateConfigOnCreate(config);
        configRepository.insert(config);

        return toConfigVO(config);
    }

    /**
     * 更新审批配置
     */
    @Transactional
    public ApprovalConfigVO updateConfig(UpdateApprovalConfigDTO dto) {
        ApprovalConfig config = configRepository.selectById(dto.getId());
        if (config == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "审批配置不存在");
        }

        if (dto.getConfigName() != null) {
            config.setConfigName(dto.getConfigName());
        }
        if (dto.getApprovalMode() != null) {
            config.setApprovalMode(ApprovalMode.valueOf(dto.getApprovalMode()));
        }
        if (dto.getApproverRoleIds() != null) {
            config.setApproverRoleIds(dto.getApproverRoleIds());
        }
        if (dto.getEnabled() != null) {
            config.setEnabled(dto.getEnabled());
        }

        domainService.validateConfigOnUpdate(config);
        configRepository.updateById(config);

        return toConfigVO(config);
    }

    /**
     * 删除审批配置
     */
    @Transactional
    public void deleteConfig(Long configId) {
        domainService.validateConfigOnDelete(configId);
        configRepository.deleteById(configId);
    }

    /**
     * 查询审批配置详情
     */
    public ApprovalConfigVO getConfig(Long configId) {
        ApprovalConfig config = configRepository.selectById(configId);
        if (config == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "审批配置不存在");
        }
        return toConfigVO(config);
    }

    /**
     * 查询所有审批配置列表
     */
    public List<ApprovalConfigVO> listAllConfigs() {
        return configRepository.selectAll().stream()
                .map(this::toConfigVO)
                .toList();
    }

    // ========== 审批操作 ==========

    /**
     * 提交审批
     *
     * @return true=需要人工审批，false=自动通过
     */
    @Transactional
    public boolean submitForApproval(SubmitApprovalDTO dto) {
        Long operatorId = UserContext.getCurrentUserId();
        return domainService.submitForApproval(
                dto.getBusinessType(), dto.getBusinessId(),
                dto.getBusinessNo(), operatorId);
    }

    /**
     * 审批操作（通过或驳回）
     */
    @Transactional
    public void approve(ApproveDTO dto) {
        Long operatorId = UserContext.getCurrentUserId();
        domainService.approve(dto.getTaskId(), dto.getApproved(), dto.getOpinion(), operatorId);
    }

    // ========== 审批查询 ==========

    /**
     * 查询当前用户的待办审批列表
     */
    public List<ApprovalTaskVO> listMyPendingTasks() {
        Long currentUserId = UserContext.getCurrentUserId();
        return taskRepository.selectPendingByApproverId(currentUserId).stream()
                .map(this::toTaskVO)
                .toList();
    }

    /**
     * 查询指定业务单据的审批记录
     */
    public List<ApprovalTaskVO> listApprovalRecords(String businessType, Long businessId) {
        return taskRepository.selectByBusiness(businessType, businessId).stream()
                .map(this::toTaskVO)
                .toList();
    }

    // ========== 转换方法 ==========

    private ApprovalConfigVO toConfigVO(ApprovalConfig config) {
        ApprovalConfigVO vo = new ApprovalConfigVO();
        vo.setId(config.getId());
        vo.setBusinessType(config.getBusinessType());
        vo.setConfigName(config.getConfigName());
        vo.setApprovalMode(config.getApprovalMode().name());
        vo.setApproverRoleIds(config.getApproverRoleIds());
        vo.setEnabled(config.getEnabled());
        vo.setCreatedBy(config.getCreatedBy());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedBy(config.getUpdatedBy());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }

    private ApprovalTaskVO toTaskVO(ApprovalTask task) {
        ApprovalTaskVO vo = new ApprovalTaskVO();
        vo.setId(task.getId());
        vo.setBusinessType(task.getBusinessType());
        vo.setBusinessId(task.getBusinessId());
        vo.setBusinessNo(task.getBusinessNo());
        vo.setApprovalMode(task.getApprovalMode().name());
        vo.setStatus(task.getStatus().name());
        vo.setApproverId(task.getApproverId());
        vo.setApproverRoleId(task.getApproverRoleId());
        vo.setOpinion(task.getOpinion());
        vo.setApprovedAt(task.getApprovedAt());
        vo.setCreatedAt(task.getCreatedAt());
        return vo;
    }
}
