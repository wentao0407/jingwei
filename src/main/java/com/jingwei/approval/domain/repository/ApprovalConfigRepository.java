package com.jingwei.approval.domain.repository;

import com.jingwei.approval.domain.model.ApprovalConfig;

import java.util.List;

/**
 * 审批配置仓库接口
 * <p>
 * 定义审批配置数据的持久化操作，由 infrastructure 层实现。
 * </p>
 *
 * @author JingWei
 */
public interface ApprovalConfigRepository {

    /**
     * 根据ID查询审批配置
     *
     * @param id 主键
     * @return 审批配置实体，不存在返回 null
     */
    ApprovalConfig selectById(Long id);

    /**
     * 根据业务类型查询审批配置
     * <p>
     * 同一业务类型只允许一条生效的配置（唯一约束）。
     * </p>
     *
     * @param businessType 业务类型（如 SALES_ORDER）
     * @return 审批配置实体，不存在返回 null
     */
    ApprovalConfig selectByBusinessType(String businessType);

    /**
     * 查询所有审批配置列表
     *
     * @return 审批配置列表
     */
    List<ApprovalConfig> selectAll();

    /**
     * 新增审批配置
     *
     * @param config 审批配置实体
     * @return 影响行数
     */
    int insert(ApprovalConfig config);

    /**
     * 更新审批配置
     *
     * @param config 审批配置实体
     * @return 影响行数
     */
    int updateById(ApprovalConfig config);

    /**
     * 根据ID删除审批配置（逻辑删除）
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 检查业务类型是否已存在审批配置
     *
     * @param businessType 业务类型
     * @return true=已存在
     */
    boolean existsByBusinessType(String businessType);

    /**
     * 检查业务类型是否已存在审批配置（排除指定ID）
     *
     * @param businessType 业务类型
     * @param excludeId    排除的配置ID
     * @return true=已存在
     */
    boolean existsByBusinessTypeExcludeId(String businessType, Long excludeId);
}
