package com.jingwei.approval.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 审批配置实体
 * <p>
 * 对应数据库表 t_sys_approval_config，定义什么业务类型需要谁审批。
 * 审批配置是业务规则，不是流程实例，每个业务类型只允许一条生效的配置。
 * </p>
 * <p>
 * 两种审批模式：
 * <ul>
 *   <li>SINGLE（单人审批）：approverRoleIds 包含一个角色ID，
 *       该角色下任一用户均可审批</li>
 *   <li>OR_SIGN（或签）：approverRoleIds 包含多个角色ID，
 *       所有角色下的用户都会收到待办，任意一人审批即通过</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_sys_approval_config", autoResultMap = true)
public class ApprovalConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 业务类型（如 SALES_ORDER、PURCHASE_ORDER） */
    private String businessType;

    /** 配置名称 */
    private String configName;

    /** 审批模式：SINGLE（单人）/ OR_SIGN（或签） */
    private ApprovalMode approvalMode;

    /** 审批人角色ID列表（JSONB），单人模式含1个角色，或签模式含多个角色 */
    @TableField(typeHandler = com.jingwei.common.config.JsonbTypeHandler.class)
    private List<Long> approverRoleIds;

    /** 是否启用 */
    private Boolean enabled;
}
