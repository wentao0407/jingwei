package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.SeasonStatus;
import com.jingwei.master.domain.model.SeasonType;
import com.jingwei.master.domain.model.Season;
import com.jingwei.master.domain.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 季节仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 SeasonRepository 接口。
 * 查询操作使用 LambdaQueryWrapper 构建条件，保证类型安全。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SeasonRepositoryImpl implements SeasonRepository {

    private final SeasonMapper seasonMapper;

    @Override
    public Season selectById(Long id) {
        return seasonMapper.selectById(id);
    }

    @Override
    public List<Season> selectAll() {
        return seasonMapper.selectList(
                new LambdaQueryWrapper<Season>()
                        .orderByDesc(Season::getYear)
                        .orderByAsc(Season::getSeasonType));
    }

    @Override
    public List<Season> selectByCondition(Integer year, String seasonType, String status) {
        LambdaQueryWrapper<Season> wrapper = new LambdaQueryWrapper<>();

        if (year != null) {
            wrapper.eq(Season::getYear, year);
        }
        if (seasonType != null) {
            wrapper.eq(Season::getSeasonType, SeasonType.valueOf(seasonType));
        }
        if (status != null) {
            wrapper.eq(Season::getStatus, SeasonStatus.valueOf(status));
        }

        wrapper.orderByDesc(Season::getYear)
               .orderByAsc(Season::getSeasonType);
        return seasonMapper.selectList(wrapper);
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        LambdaQueryWrapper<Season> wrapper = new LambdaQueryWrapper<Season>()
                .eq(Season::getCode, code);

        if (excludeId != null) {
            wrapper.ne(Season::getId, excludeId);
        }

        return seasonMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByYearAndType(Integer year, String seasonType, Long excludeId) {
        LambdaQueryWrapper<Season> wrapper = new LambdaQueryWrapper<Season>()
                .eq(Season::getYear, year)
                .eq(Season::getSeasonType, SeasonType.valueOf(seasonType));

        if (excludeId != null) {
            wrapper.ne(Season::getId, excludeId);
        }

        return seasonMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int insert(Season season) {
        return seasonMapper.insert(season);
    }

    @Override
    public int updateById(Season season) {
        return seasonMapper.updateById(season);
    }

    @Override
    public int deleteById(Long id) {
        return seasonMapper.deleteById(id);
    }
}
