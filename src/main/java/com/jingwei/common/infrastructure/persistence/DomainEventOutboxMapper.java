package com.jingwei.common.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.common.domain.model.DomainEventOutbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 领域事件 Outbox Mapper
 *
 * @author JingWei
 */
@Mapper
public interface DomainEventOutboxMapper extends BaseMapper<DomainEventOutbox> {

    /**
     * 标记事件已投递成功
     *
     * @param id 事件ID
     * @return 影响行数
     */
    @Update("UPDATE t_domain_event_outbox SET published = TRUE, published_at = NOW(), " +
            "updated_at = NOW() WHERE id = #{id} AND deleted = FALSE")
    int markPublished(@Param("id") Long id);

    /**
     * 标记事件投递失败（增加重试计数，记录失败原因）
     *
     * @param id           事件ID
     * @param errorMessage 失败原因
     * @return 影响行数
     */
    @Update("UPDATE t_domain_event_outbox SET retry_count = retry_count + 1, " +
            "error_message = #{errorMessage}, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted = FALSE")
    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}
