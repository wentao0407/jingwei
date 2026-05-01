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
}
