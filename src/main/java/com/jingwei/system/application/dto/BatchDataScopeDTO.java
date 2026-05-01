package com.jingwei.system.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 批量配置数据权限 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class BatchDataScopeDTO {

    List<ConfigureDataScopeDTO> scopes;
}
