package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.Size;
import com.jingwei.master.domain.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 尺码仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 SizeRepository 接口。
 * 尺码从属于尺码组，所有操作都需关联 sizeGroupId。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SizeRepositoryImpl implements SizeRepository {

    private final SizeMapper sizeMapper;

    @Override
    public Size selectById(Long id) {
        return sizeMapper.selectById(id);
    }

    @Override
    public List<Size> selectBySizeGroupId(Long sizeGroupId) {
        return sizeMapper.selectList(
                new LambdaQueryWrapper<Size>()
                        .eq(Size::getSizeGroupId, sizeGroupId)
                        .orderByAsc(Size::getSortOrder));
    }

    @Override
    public boolean existsBySizeGroupIdAndCode(Long sizeGroupId, String code, Long excludeId) {
        LambdaQueryWrapper<Size> wrapper = new LambdaQueryWrapper<Size>()
                .eq(Size::getSizeGroupId, sizeGroupId)
                .eq(Size::getCode, code);

        if (excludeId != null) {
            wrapper.ne(Size::getId, excludeId);
        }

        return sizeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int getMaxSortOrder(Long sizeGroupId) {
        // 查询该尺码组下所有尺码，取最大排序号
        // 数据量通常在 10 以内，全量查询无性能问题
        List<Size> sizes = sizeMapper.selectList(
                new LambdaQueryWrapper<Size>()
                        .eq(Size::getSizeGroupId, sizeGroupId)
                        .select(Size::getSortOrder));

        return sizes.stream()
                .mapToInt(Size::getSortOrder)
                .max()
                .orElse(0);
    }

    @Override
    public int insert(Size size) {
        return sizeMapper.insert(size);
    }

    @Override
    public int updateById(Size size) {
        return sizeMapper.updateById(size);
    }

    @Override
    public int deleteById(Long id) {
        return sizeMapper.deleteById(id);
    }

    @Override
    public long countBySizeGroupId(Long sizeGroupId) {
        return sizeMapper.selectCount(
                new LambdaQueryWrapper<Size>()
                        .eq(Size::getSizeGroupId, sizeGroupId));
    }
}
