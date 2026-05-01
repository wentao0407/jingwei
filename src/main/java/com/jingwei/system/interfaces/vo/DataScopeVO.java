package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据权限规则 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class DataScopeVO {

    private Long id;
    private Long roleId;
    private String scopeType;
    private String scopeValue;
}
