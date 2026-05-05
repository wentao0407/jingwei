package com.jingwei.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL JSONB 类型处理器
 * <p>
 * 解决 MyBatis-Plus 内置 JacksonTypeHandler 在 PostgreSQL 上的兼容性问题：
 * JacksonTypeHandler 将 Java 对象序列化为 JSON 字符串后，使用
 * PreparedStatement#setString() 绑定参数，PostgreSQL 检测到参数类型为
 * character varying 而非 jsonb，抛出异常：
 * <pre>column "ext_attrs" is of type jsonb but expression is of type character varying</pre>
 * </p>
 * <p>
 * 本处理器使用 {@link PGobject} 包装 JSON 字符串并指定 type="jsonb"，
 * 使 PostgreSQL 正确识别参数类型，同时读取时将 jsonb 值反序列化为 Java 对象。
 * </p>
 * <p>
 * 用法：在实体字段上标注 {@code @TableField(typeHandler = JsonbTypeHandler.class)}
 * </p>
 *
 * @param <T> Java 对象类型（如 Map、List、POJO 等）
 * @author JingWei
 */
@MappedTypes(Object.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonbTypeHandler<T> extends BaseTypeHandler<T> {

    /** Jackson 序列化/反序列化器（线程安全，全局复用） */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Class<T> type;

    public JsonbTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
            throws SQLException {
        // 使用 PGobject 包装 JSON 字符串，指定 type="jsonb"
        // 这样 PostgreSQL 能正确识别参数类型，避免 "character varying" 错误
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        try {
            pgObject.setValue(OBJECT_MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize object to JSON: " + parameter, e);
        }
        ps.setObject(i, pgObject);
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJsonb(rs.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJsonb(rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJsonb(cs.getString(columnIndex));
    }

    /**
     * 将 JSONB 字符串反序列化为 Java 对象
     *
     * @param json JSONB 字符串值
     * @return 反序列化后的 Java 对象，null 输入返回 null
     */
    private T parseJsonb(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize JSON to " + type.getName() + ": " + json, e);
        }
    }
}
