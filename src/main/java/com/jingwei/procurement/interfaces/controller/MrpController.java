package com.jingwei.procurement.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.procurement.application.dto.MrpCalculateDTO;
import com.jingwei.procurement.application.dto.MrpQueryDTO;
import com.jingwei.procurement.application.service.MrpApplicationService;
import com.jingwei.procurement.interfaces.vo.MrpCalculateResultVO;
import com.jingwei.procurement.interfaces.vo.MrpResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * MRP 计算 Controller
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MrpController {

    private final MrpApplicationService mrpApplicationService;

    /**
     * 执行 MRP 计算
     */
    @RequirePermission("procurement:mrp:calculate")
    @PostMapping("/procurement/mrp/calculate")
    public R<MrpCalculateResultVO> calculate(@Valid @RequestBody MrpCalculateDTO dto) {
        return R.ok(mrpApplicationService.calculate(dto));
    }

    /**
     * 分页查询 MRP 结果
     */
    @PostMapping("/procurement/mrp/results")
    public R<IPage<MrpResultVO>> pageQuery(@Valid @RequestBody MrpQueryDTO dto) {
        return R.ok(mrpApplicationService.pageQuery(dto));
    }
}
