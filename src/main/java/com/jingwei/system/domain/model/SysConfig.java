package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统配置实体
 * <p>
 * 对应数据库表 t_sys_config，管理键值对形式的系统全局参数。
 * 修改配置时 remark 字段记录修改原因。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_config")
public class SysConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 配置键（全局唯一） */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置分组：INVENTORY/PASSWORD/MRP/OTHER/DEFAULT */
    private String configGroup;

    /** 配置说明 */
    private String description;

    /** 修改后是否需要重启服务 */
    private Boolean needRestart;

    /** 修改原因 */
    private String remark;
}
