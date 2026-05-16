package com.jingwei.warehouse.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MaterialReturnVO {
    private Long id;
    private String returnNo;
    private Long productionOrderId;
    private String status;
    private String statusLabel;
    private String remark;
    private LocalDateTime createdAt;
    private List<MaterialReturnLineVO> lines;

    @Getter
    @Setter
    public static class MaterialReturnLineVO {
        private Long id;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private String batchNo;
        private BigDecimal quantity;
        private String unit;
        private String remark;
    }
}
