package com.jingwei.master.interfaces.controller;

import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.CreateSizeDTO;
import com.jingwei.master.application.dto.CreateSizeGroupDTO;
import com.jingwei.master.application.dto.UpdateSizeDTO;
import com.jingwei.master.application.dto.UpdateSizeGroupDTO;
import com.jingwei.master.application.service.SizeGroupApplicationService;
import com.jingwei.master.interfaces.vo.SizeGroupVO;
import com.jingwei.master.interfaces.vo.SizeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 尺码组管理 Controller
 * <p>
 * 提供尺码组和尺码的 CRUD 接口。
 * 所有接口统一使用 POST 方法。
 * 尺码从属于尺码组，尺码操作通过 /master/size-group/{groupId}/size 系列路径访问。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SizeGroupController {

    private final SizeGroupApplicationService sizeGroupApplicationService;

    // ==================== 尺码组接口 ====================

    /**
     * 创建尺码组
     */
    @RequirePermission("master:sizeGroup:create")
    @PostMapping("/master/size-group/create")
    public R<SizeGroupVO> createSizeGroup(@Valid @RequestBody CreateSizeGroupDTO dto) {
        return R.ok(sizeGroupApplicationService.createSizeGroup(dto));
    }

    /**
     * 更新尺码组
     */
    @RequirePermission("master:sizeGroup:update")
    @PostMapping("/master/size-group/update")
    public R<SizeGroupVO> updateSizeGroup(@RequestParam Long sizeGroupId,
                                          @Valid @RequestBody UpdateSizeGroupDTO dto) {
        return R.ok(sizeGroupApplicationService.updateSizeGroup(sizeGroupId, dto));
    }

    /**
     * 删除尺码组（同时删除组内所有尺码）
     * <p>
     * 被SPU引用的尺码组不可删除，但可停用。
     * </p>
     */
    @RequirePermission("master:sizeGroup:delete")
    @PostMapping("/master/size-group/delete")
    public R<Void> deleteSizeGroup(@RequestParam Long sizeGroupId) {
        sizeGroupApplicationService.deleteSizeGroup(sizeGroupId);
        return R.ok();
    }

    /**
     * 查询尺码组列表（支持按品类和状态筛选，不含尺码详情）
     */
    @PostMapping("/master/size-group/list")
    public R<List<SizeGroupVO>> listSizeGroups(@RequestParam(required = false) String category,
                                               @RequestParam(required = false) String status) {
        return R.ok(sizeGroupApplicationService.listSizeGroups(category, status));
    }

    /**
     * 查询尺码组详情（含尺码列表）
     */
    @PostMapping("/master/size-group/detail")
    public R<SizeGroupVO> getSizeGroupDetail(@RequestParam Long sizeGroupId) {
        return R.ok(sizeGroupApplicationService.getSizeGroupDetail(sizeGroupId));
    }

    // ==================== 尺码接口 ====================

    /**
     * 在尺码组下新增尺码
     * <p>
     * 已被引用的尺码组也允许新增尺码（追加到末尾）。
     * </p>
     */
    @RequirePermission("master:sizeGroup:create")
    @PostMapping("/master/size-group/size/create")
    public R<SizeVO> createSize(@RequestParam Long sizeGroupId,
                                @Valid @RequestBody CreateSizeDTO dto) {
        return R.ok(sizeGroupApplicationService.createSize(sizeGroupId, dto));
    }

    /**
     * 更新尺码
     * <p>
     * 已被引用的尺码组内，不可修改尺码编码。
     * </p>
     */
    @RequirePermission("master:sizeGroup:update")
    @PostMapping("/master/size-group/size/update")
    public R<SizeVO> updateSize(@RequestParam Long sizeId,
                                @Valid @RequestBody UpdateSizeDTO dto) {
        return R.ok(sizeGroupApplicationService.updateSize(sizeId, dto));
    }

    /**
     * 删除尺码
     * <p>
     * 已被引用的尺码组内，不可删除尺码。
     * </p>
     */
    @RequirePermission("master:sizeGroup:delete")
    @PostMapping("/master/size-group/size/delete")
    public R<Void> deleteSize(@RequestParam Long sizeId) {
        sizeGroupApplicationService.deleteSize(sizeId);
        return R.ok();
    }
}
