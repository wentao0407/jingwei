package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料分类 Mapper
 * <p>
 * 基于 MyBatis-Plus BaseMapper，提供基础的 CRUD 能力。
 * </p>
 *
 * @author JingWei
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
