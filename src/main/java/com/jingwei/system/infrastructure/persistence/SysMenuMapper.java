package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.system.domain.model.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单 Mapper 接口
 *
 * @author JingWei
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
