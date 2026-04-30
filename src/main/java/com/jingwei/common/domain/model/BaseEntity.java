package com.jingwei.common.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有业务实体必须继承此类，统一包含审计字段和乐观锁字段。
 * 审计字段由 {@link com.jingwei.common.config.JingWeiMetaObjectHandler} 自动填充。
 * </p>
 * <ul>
 *   <li>id — 主键，雪花算法生成</li>
 *   <li>createdBy — 创建人ID，插入时自动填充</li>
 *   <li>createdAt — 创建时间，插入时自动填充</li>
 *   <li>updatedBy — 最后修改人ID，插入和更新时自动填充</li>
 *   <li>updatedAt — 最后修改时间，插入和更新时自动填充</li>
 *   <li>deleted — 软删除标记，默认 false，全局逻辑删除</li>
 *   <li>version — 乐观锁版本号，MyBatis-Plus 自动递增</li>
 * </ul>
 *
 * @author JingWei
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，雪花算法生成 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最后修改人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /** 最后修改时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除标记：false=未删除，true=已删除 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;

    /** 乐观锁版本号 */
    @Version
    private Integer version = 0;
}
