package com.jingwei.order.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingwei.order.domain.model.SizeMatrix.SizeEntry;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * SizeMatrix JSONB 类型处理器
 * <p>
 * 将 {@link SizeMatrix} 值对象与 PostgreSQL JSONB 字段互转。
 * 复用 {@link com.jingwei.common.config.JsonbTypeHandler} 的 PGobject 包装方案，
 * 解决 PostgreSQL "character varying" 类型不匹配问题。
 * </p>
 * <p>
 * JSONB 存储结构：
 * <pre>
 * {
 *   "sizeGroupId": 1,
 *   "sizes": [
 *     {"sizeId": 10, "code": "S", "quantity": 100},
 *     {"sizeId": 11, "code": "M", "quantity": 200}
 *   ],
 *   "totalQuantity": 300
 * }
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@MappedTypes(SizeMatrix.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class SizeMatrixTypeHandler extends BaseTypeHandler<SizeMatrix> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SizeMatrix parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        try {
            pgObject.setValue(OBJECT_MAPPER.writeValueAsString(toMap(parameter)));
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize SizeMatrix to JSON: " + parameter, e);
        }
        ps.setObject(i, pgObject);
    }

    @Override
    public SizeMatrix getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseSizeMatrix(rs.getString(columnName));
    }

    @Override
    public SizeMatrix getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseSizeMatrix(rs.getString(columnIndex));
    }

    @Override
    public SizeMatrix getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseSizeMatrix(cs.getString(columnIndex));
    }

    /**
     * 将 SizeMatrix 转为可序列化的 Map 结构
     *
     * @param matrix 尺码矩阵
     * @return Map 表示
     */
    private Map<String, Object> toMap(SizeMatrix matrix) {
        return Map.of(
                "sizeGroupId", matrix.getSizeGroupId(),
                "sizes", matrix.getSizes(),
                "totalQuantity", matrix.getTotalQuantity()
        );
    }

    /**
     * 将 JSONB 字符串反序列化为 SizeMatrix
     *
     * @param json JSONB 字符串
     * @return SizeMatrix 实例，null 输入返回 null
     */
    private SizeMatrix parseSizeMatrix(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
            Long sizeGroupId = ((Number) map.get("sizeGroupId")).longValue();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sizesRaw = (List<Map<String, Object>>) map.get("sizes");

            List<SizeEntry> sizes = sizesRaw.stream()
                    .map(s -> new SizeEntry(
                            ((Number) s.get("sizeId")).longValue(),
                            (String) s.get("code"),
                            ((Number) s.get("quantity")).intValue()
                    ))
                    .toList();

            return new SizeMatrix(sizeGroupId, sizes);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize JSON to SizeMatrix: " + json, e);
        }
    }
}
