package com.jingwei.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonConfigTest {

    @Test
    @DisplayName("Long类型应序列化为字符串避免前端雪花ID精度丢失")
    void shouldSerializeLongAsString() throws Exception {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().longToStringCustomizer().customize(builder);
        ObjectMapper objectMapper = builder.build();

        String json = objectMapper.writeValueAsString(Map.of("id", 1778059952652742657L));

        assertEquals("{\"id\":\"1778059952652742657\"}", json);
    }
}
