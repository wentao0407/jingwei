package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 尺码组响应 VO
 * <p>
 * 返回给前端的尺码组数据，包含关联的尺码列表。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class SizeGroupVO {

    /** 尺码组ID */
    private Long id;

    /** 尺码组编码 */
    private String code;

    /** 尺码组名称 */
    private String name;

    /** 适用品类：WOMEN/MEN/CHILDREN */
    private String category;

    /** 状态：ACTIVE/INACTIVE */
    private String status;

    /** 尺码列表（按 sort_order 排序） */
    private List<SizeVO> sizes;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
