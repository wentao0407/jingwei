package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 属性定义 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AttributeDefinitionVO {

    private Long id;
    private String code;
    private String name;
    private String materialType;
    private String inputType;
    private Boolean required;
    private Integer sortOrder;
    private List<String> options;
    private String jsonbPath;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
