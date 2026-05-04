package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 到货通知单行 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AsnLineVO {

    private Long id;
    private Long asnId;
    private Long procurementLineId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal expectedQuantity;
    private BigDecimal receivedQuantity;
    private String qcStatus;
    private String qcStatusLabel;
    private BigDecimal acceptedQuantity;
    private BigDecimal rejectedQuantity;
    private String batchNo;
    private String remark;

    /** 检验结果详情 */
    private QcResultVO qcResult;

    /**
     * 检验结果 VO
     */
    @Getter
    @Setter
    public static class QcResultVO {

        private String inspector;
        private String inspectedAt;
        private List<QcItemVO> items;
        private String overallResult;
        private String conclusion;
    }

    /**
     * 检验项 VO
     */
    @Getter
    @Setter
    public static class QcItemVO {

        private String name;
        private String standard;
        private String actual;
        private String result;
    }
}
