package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 库位实体
 * <p>
 * 对应数据库表 t_md_location，管理仓库下的库位信息。
 * 库位按"库区-货架-层-位"四级编码，完整编码自动拼接。
 * FROZEN 状态的库位不可进行出入库操作。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_location")
public class Location extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 仓库ID */
    private Long warehouseId;

    /** 库区编码（如 A） */
    private String zoneCode;

    /** 货架编码（如 01） */
    private String rackCode;

    /** 层编码（如 02） */
    private String rowCode;

    /** 位编码（如 03） */
    private String binCode;

    /** 完整编码（如 WH01-A-01-02-03，自动拼接） */
    private String fullCode;

    /** 库位类型：STORAGE/PICKING/STAGING/QC */
    private LocationType locationType;

    /** 容量（可存放的件数或储位单位） */
    private Integer capacity;

    /** 已用容量（上架/移库时更新，默认0） */
    private Integer usedCapacity;

    /** 状态：ACTIVE/INACTIVE/FROZEN */
    private LocationStatus status;

    /** 备注 */
    private String remark;
}
