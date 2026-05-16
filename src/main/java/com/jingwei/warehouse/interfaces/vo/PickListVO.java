package com.jingwei.warehouse.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PickListVO {
    private Long id;
    private Long waveId;
    private String pickListNo;
    private Long pickerId;
    private String status;
    private String remark;
    private List<PickItemVO> items;
}
