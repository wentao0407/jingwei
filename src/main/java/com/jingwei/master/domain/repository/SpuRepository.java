package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Spu;

import java.util.List;

/**
 * SPU 款式仓库接口
 *
 * @author JingWei
 */
public interface SpuRepository {

    Spu selectById(Long id);

    List<Spu> selectAll();

    List<Spu> selectByCondition(String status, Long seasonId, Long categoryId);

    boolean existsByCode(String code, Long excludeId);

    int insert(Spu spu);

    int updateById(Spu spu);

    int deleteById(Long id);

    /**
     * 统计引用指定尺码组的 SPU 数量
     *
     * @param sizeGroupId 尺码组ID
     * @return 引用该尺码组的 SPU 数量
     */
    long countBySizeGroupId(Long sizeGroupId);
}
