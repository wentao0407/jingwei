package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 追加颜色请求 DTO
 * <p>
 * SPU 创建后追加新颜色，系统增量生成对应的 Color-way 和 SKU。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class AddColorDTO {

    /** 新增颜色列表（至少一个） */
    @NotEmpty(message = "至少选择一个颜色")
    private List<ColorItemDTO> colors;
}
