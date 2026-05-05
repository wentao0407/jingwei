package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.order.domain.model.ReturnOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退货单 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ReturnOrderMapper extends BaseMapper<ReturnOrder> {
}
