package com.jingwei.procurement.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.procurement.domain.model.Bom;
import com.jingwei.procurement.domain.model.BomItem;
import com.jingwei.procurement.domain.model.BomStatus;
import com.jingwei.procurement.domain.repository.BomItemRepository;
import com.jingwei.procurement.domain.repository.BomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BOM 领域服务
 * <p>
 * 负责 BOM 的创建、编辑、审批、版本控制等核心业务逻辑。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BomDomainService {

    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;

    /**
     * 创建 BOM
     * <p>
     * 自动递增版本号，初始状态为 DRAFT。
     * </p>
     *
     * @param bom  BOM 主表
     * @param items BOM 行列表
     * @return 保存后的 BOM
     */
    @Transactional(rollbackFor = Exception.class)
    public Bom createBom(Bom bom, List<BomItem> items) {
        // 自动递增版本号
        Integer maxVersion = bomRepository.selectMaxBomVersion(bom.getSpuId());
        int newVersion = (maxVersion != null) ? maxVersion + 1 : 1;
        bom.setBomVersion(newVersion);
        bom.setStatus(BomStatus.DRAFT);

        bomRepository.insert(bom);

        // 保存行
        for (int i = 0; i < items.size(); i++) {
            BomItem item = items.get(i);
            item.setBomId(bom.getId());
            item.setSortOrder(i + 1);
            bomItemRepository.insert(item);
        }

        bom.setItems(items);
        log.info("创建BOM: id={}, spuId={}, bomVersion={}", bom.getId(), bom.getSpuId(), newVersion);
        return bom;
    }

    /**
     * 编辑 BOM（仅 DRAFT 状态允许）
     *
     * @param bomId BOM ID
     * @param updatedBom 更新的主表信息
     * @param items 更新的行列表（全量替换）
     * @param operatorId 操作人ID
     * @return 更新后的 BOM
     */
    @Transactional(rollbackFor = Exception.class)
    public Bom updateBom(Long bomId, Bom updatedBom, List<BomItem> items, Long operatorId) {
        Bom existing = bomRepository.selectById(bomId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (existing.getStatus() != BomStatus.DRAFT) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 更新主表字段
        if (updatedBom.getEffectiveFrom() != null) {
            existing.setEffectiveFrom(updatedBom.getEffectiveFrom());
        }
        if (updatedBom.getEffectiveTo() != null) {
            existing.setEffectiveTo(updatedBom.getEffectiveTo());
        }
        if (updatedBom.getRemark() != null) {
            existing.setRemark(updatedBom.getRemark());
        }
        bomRepository.updateById(existing);

        // 全量替换行：删除旧行，插入新行
        bomItemRepository.deleteByBomId(bomId);
        for (int i = 0; i < items.size(); i++) {
            BomItem item = items.get(i);
            item.setBomId(bomId);
            item.setSortOrder(i + 1);
            bomItemRepository.insert(item);
        }

        existing.setItems(items);
        log.info("编辑BOM: id={}", bomId);
        return existing;
    }

    /**
     * 删除 BOM（仅 DRAFT 状态允许，且不能被生产订单引用）
     *
     * @param bomId BOM ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBom(Long bomId) {
        Bom bom = bomRepository.selectById(bomId);
        if (bom == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (bom.getStatus() != BomStatus.DRAFT) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED);
        }
        if (bomRepository.isReferencedByProductionOrder(bomId)) {
            throw new BizException(ErrorCode.BOM_REFERENCED);
        }

        bomItemRepository.deleteByBomId(bomId);
        bomRepository.deleteById(bomId);
        log.info("删除BOM: id={}", bomId);
    }

    /**
     * 审批 BOM
     * <p>
     * 审批通过后：
     * 1. 当前 BOM 状态变为 APPROVED
     * 2. 同 SPU 的旧 APPROVED 版本自动标记为 OBSOLETE
     * </p>
     *
     * @param bomId BOM ID
     * @param approverId 审批人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveBom(Long bomId, Long approverId) {
        Bom bom = bomRepository.selectById(bomId);
        if (bom == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (bom.getStatus() != BomStatus.DRAFT) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 将同 SPU 的旧 APPROVED 版本标记为 OBSOLETE
        bomRepository.selectApprovedBySpuId(bom.getSpuId())
                .ifPresent(oldBom -> {
                    oldBom.setStatus(BomStatus.OBSOLETE);
                    oldBom.setEffectiveTo(LocalDate.now());
                    bomRepository.updateById(oldBom);
                    log.info("旧BOM版本已淘汰: id={}, spuId={}", oldBom.getId(), oldBom.getSpuId());
                });

        // 审批当前 BOM
        bom.setStatus(BomStatus.APPROVED);
        bom.setApprovedBy(approverId);
        bom.setApprovedAt(LocalDateTime.now());
        if (bom.getEffectiveFrom() == null) {
            bom.setEffectiveFrom(LocalDate.now());
        }
        bomRepository.updateById(bom);

        log.info("审批BOM: id={}, spuId={}, bomVersion={}", bomId, bom.getSpuId(), bom.getBomVersion());
    }

    /**
     * 查询 BOM 详情
     *
     * @param bomId BOM ID
     * @return BOM（含行）
     */
    public Bom getBomDetail(Long bomId) {
        Bom bom = bomRepository.selectDetailById(bomId);
        if (bom == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        return bom;
    }
}
