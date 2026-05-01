package com.jingwei.master.application.service;

import com.jingwei.master.application.dto.CreateSizeDTO;
import com.jingwei.master.application.dto.CreateSizeGroupDTO;
import com.jingwei.master.application.dto.UpdateSizeDTO;
import com.jingwei.master.application.dto.UpdateSizeGroupDTO;
import com.jingwei.master.domain.model.Size;
import com.jingwei.master.domain.model.SizeCategory;
import com.jingwei.master.domain.model.SizeGroup;
import com.jingwei.master.domain.service.SizeGroupDomainService;
import com.jingwei.master.interfaces.vo.SizeGroupVO;
import com.jingwei.master.interfaces.vo.SizeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 尺码组应用服务
 * <p>
 * 负责尺码组和尺码 CRUD 的编排和事务边界管理。
 * 业务逻辑委托给 SizeGroupDomainService，本层只负责 DTO↔实体转换和事务控制。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SizeGroupApplicationService {

    private final SizeGroupDomainService sizeGroupDomainService;

    // ==================== 尺码组操作 ====================

    /**
     * 创建尺码组
     */
    @Transactional(rollbackFor = Exception.class)
    public SizeGroupVO createSizeGroup(CreateSizeGroupDTO dto) {
        SizeGroup sizeGroup = new SizeGroup();
        sizeGroup.setCode(dto.getCode());
        sizeGroup.setName(dto.getName());
        sizeGroup.setCategory(SizeCategory.valueOf(dto.getCategory()));

        SizeGroup saved = sizeGroupDomainService.createSizeGroup(sizeGroup);
        return toSizeGroupVO(saved);
    }

    /**
     * 更新尺码组
     */
    @Transactional(rollbackFor = Exception.class)
    public SizeGroupVO updateSizeGroup(Long sizeGroupId, UpdateSizeGroupDTO dto) {
        SizeGroup updated = sizeGroupDomainService.updateSizeGroup(
                sizeGroupId, dto.getName(), dto.getCategory(), dto.getStatus());
        return toSizeGroupVO(updated);
    }

    /**
     * 删除尺码组（同时删除组内所有尺码）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSizeGroup(Long sizeGroupId) {
        sizeGroupDomainService.deleteSizeGroup(sizeGroupId);
    }

    /**
     * 查询尺码组列表（按条件筛选，不含尺码详情）
     */
    public List<SizeGroupVO> listSizeGroups(String category, String status) {
        List<SizeGroup> sizeGroups = sizeGroupDomainService.listSizeGroups(category, status);
        return sizeGroups.stream().map(this::toSizeGroupVO).toList();
    }

    /**
     * 查询尺码组详情（含尺码列表）
     */
    public SizeGroupVO getSizeGroupDetail(Long sizeGroupId) {
        SizeGroup sizeGroup = sizeGroupDomainService.getSizeGroupDetail(sizeGroupId);
        return toSizeGroupVOWithSizes(sizeGroup);
    }

    // ==================== 尺码操作 ====================

    /**
     * 在尺码组下新增尺码
     */
    @Transactional(rollbackFor = Exception.class)
    public SizeVO createSize(Long sizeGroupId, CreateSizeDTO dto) {
        Size size = new Size();
        size.setCode(dto.getCode());
        size.setName(dto.getName());
        size.setSortOrder(dto.getSortOrder());

        Size saved = sizeGroupDomainService.createSize(sizeGroupId, size);
        return toSizeVO(saved);
    }

    /**
     * 更新尺码
     */
    @Transactional(rollbackFor = Exception.class)
    public SizeVO updateSize(Long sizeId, UpdateSizeDTO dto) {
        Size updated = sizeGroupDomainService.updateSize(
                sizeId, dto.getCode(), dto.getName(), dto.getSortOrder());
        return toSizeVO(updated);
    }

    /**
     * 删除尺码
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSize(Long sizeId) {
        sizeGroupDomainService.deleteSize(sizeId);
    }

    // ==================== 转换方法 ====================

    /**
     * 将 SizeGroup 实体转换为 SizeGroupVO（不含尺码列表）
     *
     * @param sizeGroup 尺码组实体
     * @return 尺码组 VO
     */
    private SizeGroupVO toSizeGroupVO(SizeGroup sizeGroup) {
        SizeGroupVO vo = new SizeGroupVO();
        vo.setId(sizeGroup.getId());
        vo.setCode(sizeGroup.getCode());
        vo.setName(sizeGroup.getName());
        vo.setCategory(sizeGroup.getCategory().name());
        vo.setStatus(sizeGroup.getStatus().name());
        vo.setCreatedAt(sizeGroup.getCreatedAt());
        vo.setUpdatedAt(sizeGroup.getUpdatedAt());
        return vo;
    }

    /**
     * 将 SizeGroup 实体转换为 SizeGroupVO（含尺码列表）
     *
     * @param sizeGroup 尺码组实体（含 sizes）
     * @return 尺码组 VO（含尺码列表）
     */
    private SizeGroupVO toSizeGroupVOWithSizes(SizeGroup sizeGroup) {
        SizeGroupVO vo = toSizeGroupVO(sizeGroup);

        if (sizeGroup.getSizes() != null && !sizeGroup.getSizes().isEmpty()) {
            vo.setSizes(sizeGroup.getSizes().stream().map(this::toSizeVO).toList());
        }

        return vo;
    }

    /**
     * 将 Size 实体转换为 SizeVO
     *
     * @param size 尺码实体
     * @return 尺码 VO
     */
    private SizeVO toSizeVO(Size size) {
        SizeVO vo = new SizeVO();
        vo.setId(size.getId());
        vo.setSizeGroupId(size.getSizeGroupId());
        vo.setCode(size.getCode());
        vo.setName(size.getName());
        vo.setSortOrder(size.getSortOrder());
        vo.setCreatedAt(size.getCreatedAt());
        vo.setUpdatedAt(size.getUpdatedAt());
        return vo;
    }
}
