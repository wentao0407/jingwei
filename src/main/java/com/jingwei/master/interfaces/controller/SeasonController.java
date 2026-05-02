package com.jingwei.master.interfaces.controller;

import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.CreateSeasonDTO;
import com.jingwei.master.application.dto.CreateWaveDTO;
import com.jingwei.master.application.dto.UpdateSeasonDTO;
import com.jingwei.master.application.dto.UpdateWaveDTO;
import com.jingwei.master.application.service.SeasonApplicationService;
import com.jingwei.master.interfaces.vo.SeasonVO;
import com.jingwei.master.interfaces.vo.WaveVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 季节管理 Controller
 * <p>
 * 提供季节和波段的 CRUD 接口。
 * 所有接口统一使用 POST 方法。
 * 波段从属于季节，波段操作通过 /master/season/wave 系列路径访问。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonApplicationService seasonApplicationService;

    // ==================== 季节接口 ====================

    /**
     * 创建季节
     */
    @RequirePermission("master:season:create")
    @PostMapping("/master/season/create")
    public R<SeasonVO> createSeason(@Valid @RequestBody CreateSeasonDTO dto) {
        return R.ok(seasonApplicationService.createSeason(dto));
    }

    /**
     * 更新季节
     */
    @RequirePermission("master:season:update")
    @PostMapping("/master/season/update")
    public R<SeasonVO> updateSeason(@RequestParam Long seasonId,
                                    @Valid @RequestBody UpdateSeasonDTO dto) {
        return R.ok(seasonApplicationService.updateSeason(seasonId, dto));
    }

    /**
     * 关闭季节
     * <p>
     * 关闭后不可在业务单据中选择，其下波段也不可选用。
     * 关闭是单向操作，不可恢复。
     * </p>
     */
    @RequirePermission("master:season:close")
    @PostMapping("/master/season/close")
    public R<Void> closeSeason(@RequestParam Long seasonId) {
        seasonApplicationService.closeSeason(seasonId);
        return R.ok();
    }

    /**
     * 删除季节（同时删除季节下的所有波段）
     */
    @RequirePermission("master:season:delete")
    @PostMapping("/master/season/delete")
    public R<Void> deleteSeason(@RequestParam Long seasonId) {
        seasonApplicationService.deleteSeason(seasonId);
        return R.ok();
    }

    /**
     * 查询季节列表（支持按年份、类型、状态筛选，不含波段详情）
     */
    @PostMapping("/master/season/list")
    public R<List<SeasonVO>> listSeasons(@RequestParam(required = false) Integer year,
                                         @RequestParam(required = false) String seasonType,
                                         @RequestParam(required = false) String status) {
        return R.ok(seasonApplicationService.listSeasons(year, seasonType, status));
    }

    /**
     * 查询季节详情（含波段列表）
     */
    @PostMapping("/master/season/detail")
    public R<SeasonVO> getSeasonDetail(@RequestParam Long seasonId) {
        return R.ok(seasonApplicationService.getSeasonDetail(seasonId));
    }

    // ==================== 波段接口 ====================

    /**
     * 在季节下新增波段
     * <p>
     * 已关闭的季节不允许新增波段。
     * </p>
     */
    @RequirePermission("master:wave:create")
    @PostMapping("/master/season/wave/create")
    public R<WaveVO> createWave(@RequestParam Long seasonId,
                                @Valid @RequestBody CreateWaveDTO dto) {
        return R.ok(seasonApplicationService.createWave(seasonId, dto));
    }

    /**
     * 更新波段
     * <p>
     * 已关闭的季节下的波段不允许修改。
     * </p>
     */
    @RequirePermission("master:wave:update")
    @PostMapping("/master/season/wave/update")
    public R<WaveVO> updateWave(@RequestParam Long waveId,
                                @Valid @RequestBody UpdateWaveDTO dto) {
        return R.ok(seasonApplicationService.updateWave(waveId, dto));
    }

    /**
     * 删除波段
     * <p>
     * 已关闭的季节下的波段不允许删除。
     * </p>
     */
    @RequirePermission("master:wave:delete")
    @PostMapping("/master/season/wave/delete")
    public R<Void> deleteWave(@RequestParam Long waveId) {
        seasonApplicationService.deleteWave(waveId);
        return R.ok();
    }
}
