package com.jingwei.warehouse.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialIssueQueryDTO {
    private int current = 1;
    private int size = 10;
    private String status;
}
