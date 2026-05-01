package com.jingwei.master.application.service;

import com.jingwei.master.application.dto.CreateSeasonDTO;
import com.jingwei.master.application.dto.CreateWaveDTO;
import com.jingwei.master.application.dto.UpdateSeasonDTO;
import com.jingwei.master.application.dto.UpdateWaveDTO;
import com.jingwei.master.domain.model.Season;
import com.jingwei.master.domain.model.SeasonType;
import com.jingwei.master.domain.model.Wave;
import com.jingwei.master.domain.service.SeasonDomainService;
import com.jingwei.master.interfaces.vo.SeasonVO;
import com.jingwei.master.interfaces.vo.WaveVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 季节应用服务
 * <p>
 * 负责季节和波段 CRUD 的编排和事务边界管理。
 * 业务逻辑委托给 SeasonDomainService，本层只负责 DTO↔实体转换和事务控制。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonApplicationService {

    private final SeasonDomainService seasonDomainService;

    // ==================== 季节操作 ====================

    /**
     * 创建季节
     */
    @Transactional(rollbackFor = Exception.class)
    public SeasonVO createSeason(CreateSeasonDTO dto) {
        Season season = new Season();
        season.setCode(dto.getCode());
        season.setName(dto.getName());
        season.setYear(dto.getYear());
        season.setSeasonType(SeasonType.valueOf(dto.getSeasonType()));
        season.setStartDate(dto.getStartDate());
        season.setEndDate(dto.getEndDate());

        Season saved = seasonDomainService.createSeason(season);
        return toSeasonVO(saved);
    }

    /**
     * 更新季节
     */
    @Transactional(rollbackFor = Exception.class)
    public SeasonVO updateSeason(Long seasonId, UpdateSeasonDTO dto) {
        Season updated = seasonDomainService.updateSeason(
                seasonId, dto.getName(), dto.getStartDate(), dto.getEndDate());
        return toSeasonVO(updated);
    }

    /**
     * 关闭季节
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeSeason(Long seasonId) {
        seasonDomainService.closeSeason(seasonId);
    }

    /**
     * 删除季节（同时删除季节下的所有波段）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeason(Long seasonId) {
        seasonDomainService.deleteSeason(seasonId);
    }

    /**
     * 查询季节列表（按条件筛选，不含波段详情）
     */
    public List<SeasonVO> listSeasons(Integer year, String seasonType, String status) {
        List<Season> seasons = seasonDomainService.listSeasons(year, seasonType, status);
        return seasons.stream().map(this::toSeasonVO).toList();
    }

    /**
     * 查询季节详情（含波段列表）
     */
    public SeasonVO getSeasonDetail(Long seasonId) {
        Season season = seasonDomainService.getSeasonDetail(seasonId);
        return toSeasonVOWithWaves(season);
    }

    // ==================== 波段操作 ====================

    /**
     * 在季节下新增波段
     */
    @Transactional(rollbackFor = Exception.class)
    public WaveVO createWave(Long seasonId, CreateWaveDTO dto) {
        Wave wave = new Wave();
        wave.setCode(dto.getCode());
        wave.setName(dto.getName());
        wave.setDeliveryDate(dto.getDeliveryDate());
        wave.setSortOrder(dto.getSortOrder());

        Wave saved = seasonDomainService.createWave(seasonId, wave);
        return toWaveVO(saved);
    }

    /**
     * 更新波段
     */
    @Transactional(rollbackFor = Exception.class)
    public WaveVO updateWave(Long waveId, UpdateWaveDTO dto) {
        Wave updated = seasonDomainService.updateWave(
                waveId, dto.getName(), dto.getDeliveryDate(), dto.getSortOrder());
        return toWaveVO(updated);
    }

    /**
     * 删除波段
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWave(Long waveId) {
        seasonDomainService.deleteWave(waveId);
    }

    // ==================== 转换方法 ====================

    /**
     * 将 Season 实体转换为 SeasonVO（不含波段列表）
     *
     * @param season 季节实体
     * @return 季节 VO
     */
    private SeasonVO toSeasonVO(Season season) {
        SeasonVO vo = new SeasonVO();
        vo.setId(season.getId());
        vo.setCode(season.getCode());
        vo.setName(season.getName());
        vo.setYear(season.getYear());
        vo.setSeasonType(season.getSeasonType().name());
        vo.setStartDate(season.getStartDate());
        vo.setEndDate(season.getEndDate());
        vo.setStatus(season.getStatus().name());
        vo.setCreatedAt(season.getCreatedAt());
        vo.setUpdatedAt(season.getUpdatedAt());
        return vo;
    }

    /**
     * 将 Season 实体转换为 SeasonVO（含波段列表）
     *
     * @param season 季节实体（含 waves）
     * @return 季节 VO（含波段列表）
     */
    private SeasonVO toSeasonVOWithWaves(Season season) {
        SeasonVO vo = toSeasonVO(season);

        if (season.getWaves() != null && !season.getWaves().isEmpty()) {
            vo.setWaves(season.getWaves().stream().map(this::toWaveVO).toList());
        }

        return vo;
    }

    /**
     * 将 Wave 实体转换为 WaveVO
     *
     * @param wave 波段实体
     * @return 波段 VO
     */
    private WaveVO toWaveVO(Wave wave) {
        WaveVO vo = new WaveVO();
        vo.setId(wave.getId());
        vo.setSeasonId(wave.getSeasonId());
        vo.setCode(wave.getCode());
        vo.setName(wave.getName());
        vo.setDeliveryDate(wave.getDeliveryDate());
        vo.setSortOrder(wave.getSortOrder());
        vo.setCreatedAt(wave.getCreatedAt());
        vo.setUpdatedAt(wave.getUpdatedAt());
        return vo;
    }
}
