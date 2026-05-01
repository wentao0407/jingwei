package com.jingwei.master.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 尺码响应 VO
 * <p>
 * 返回给前端的尺码数据，通常嵌套在 SizeGroupVO 中。
 * </p>
 *
 * @author JingWei
 */
@Data
public class SizeVO {

    /** 尺码ID */
    private Long id;

    /** 所属尺码组ID */
    private Long sizeGroupId;

    /** 尺码编码 */
    private String code;

    /** 尺码名称 */
    private String name;

    /** 排序号 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
