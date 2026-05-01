package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 数据权限规则实体
 * <p>
 * 对应数据库表 t_sys_data_scope，按角色配置数据可见范围。
 * scope_type 为维度（WAREHOUSE/DEPT/ALL），scope_value 为值（逗号分隔的ID或 ALL）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_data_scope")
public class DataScope implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long roleId;
    private String scopeType;
    private String scopeValue;
    private Long createdBy;
    private java.time.LocalDateTime createdAt;
    private Long updatedBy;
    private java.time.LocalDateTime updatedAt;
    private Boolean deleted;
    private Integer version;
}
