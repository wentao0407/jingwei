package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 编码预览VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CodePreviewVO {

    /** 预览编码（不递增流水号，使用示例值 00001） */
    private String previewCode;
}
