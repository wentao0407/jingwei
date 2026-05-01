package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.master.domain.model.SizeCategory;
import com.jingwei.master.domain.model.SizeGroup;
import com.jingwei.master.domain.repository.SizeGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 尺码组仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 SizeGroupRepository 接口。
 * 查询操作使用 LambdaQueryWrapper 构建条件，保证类型安全。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SizeGroupRepositoryImpl implements SizeGroupRepository {

    private final SizeGroupMapper sizeGroupMapper;

    @Override
    public SizeGroup selectById(Long id) {
        return sizeGroupMapper.selectById(id);
    }

    @Override
    public List<SizeGroup> selectAll() {
        return sizeGroupMapper.selectList(
                new LambdaQueryWrapper<SizeGroup>()
                        .orderByAsc(SizeGroup::getCode));
    }

    @Override
    public List<SizeGroup> selectByCondition(String category, String status) {
        LambdaQueryWrapper<SizeGroup> wrapper = new LambdaQueryWrapper<>();

        if (category != null) {
            wrapper.eq(SizeGroup::getCategory, SizeCategory.valueOf(category));
        }
        if (status != null) {
            wrapper.eq(SizeGroup::getStatus, CommonStatus.valueOf(status));
        }

        wrapper.orderByAsc(SizeGroup::getCode);
        return sizeGroupMapper.selectList(wrapper);
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        LambdaQueryWrapper<SizeGroup> wrapper = new LambdaQueryWrapper<SizeGroup>()
                .eq(SizeGroup::getCode, code);

        if (excludeId != null) {
            wrapper.ne(SizeGroup::getId, excludeId);
        }

        return sizeGroupMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int insert(SizeGroup sizeGroup) {
        return sizeGroupMapper.insert(sizeGroup);
    }

    @Override
    public int updateById(SizeGroup sizeGroup) {
        return sizeGroupMapper.updateById(sizeGroup);
    }

    @Override
    public int deleteById(Long id) {
        return sizeGroupMapper.deleteById(id);
    }
}
