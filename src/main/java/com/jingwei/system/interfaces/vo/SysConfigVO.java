package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统配置 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class SysConfigVO {

    private Long id;
    private String configKey;
    private String configValue;
    private String configGroup;
    private String description;
    private Boolean needRestart;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
