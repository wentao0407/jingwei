package com.jingwei.inventory.interfaces.controller;

import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.inventory.application.dto.AlertQueryDTO;
import com.jingwei.inventory.application.service.AlertApplicationService;
import com.jingwei.inventory.interfaces.vo.AlertVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 库存预警 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class AlertController {

    private final AlertApplicationService alertApplicationService;

    /** 手动触发预警扫描 */
    @PostMapping("/inventory/alert/scan")
    public R<Map<String, Integer>> scan() {
        int count = alertApplicationService.scanAndAlert();
        return R.ok(Map.of("generatedCount", count));
    }

    /** 确认预警 */
    @RequirePermission("inventory:alert:acknowledge")
    @PostMapping("/inventory/alert/acknowledge")
    public R<Void> acknowledge(@RequestParam Long alertId) {
        alertApplicationService.acknowledge(alertId);
        return R.ok();
    }

    /** 查询预警列表 */
    @PostMapping("/inventory/alert/list")
    public R<List<AlertVO>> listAlerts(@Valid @RequestBody AlertQueryDTO dto) {
        return R.ok(alertApplicationService.listAlerts(dto));
    }
}
