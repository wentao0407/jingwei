package com.jingwei.notification.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 通知查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class NotificationQueryDTO {

    /** 通知分类（可选） */
    private String category;

    /** 是否已读（可选） */
    private Boolean isRead;

    /** 页码（默认1） */
    private Integer pageNum = 1;

    /** 每页大小（默认20） */
    private Integer pageSize = 20;
}
