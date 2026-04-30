package com.jingwei.common.interfaces.controller;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler 单元测试
 * <p>
 * 验证全局异常处理器的三种场景：
 * <ol>
 *   <li>BizException → 正确的错误码和中文提示</li>
 *   <li>参数校验失败 → 400 + fieldErrors 列表</li>
 *   <li>未捕获异常 → 500 + "系统异常"</li>
 * </ol>
 * </p>
 *
 * @author JingWei
 */
@WebMvcTest
@ContextConfiguration(classes = {
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandlerTest.TestSecurityConfig.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试环境安全配置
     */
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    /**
     * 测试用 Controller：模拟各种异常场景
     */
    @org.springframework.web.bind.annotation.RestController
    static class TestController {

        /** 抛出 BizException */
        @org.springframework.web.bind.annotation.PostMapping("/test/biz-error")
        public Object bizError() {
            throw new BizException(ErrorCode.MATERIAL_CODE_DUPLICATE);
        }

        /** 抛出带自定义消息的 BizException */
        @org.springframework.web.bind.annotation.PostMapping("/test/biz-error-custom")
        public Object bizErrorCustom() {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料xxx不存在");
        }

        /** 参数校验 */
        @org.springframework.web.bind.annotation.PostMapping("/test/validation")
        public Object validation(@Valid TestDTO dto) {
            return "ok";
        }

        /** 抛出未捕获异常 */
        @org.springframework.web.bind.annotation.PostMapping("/test/system-error")
        public Object systemError() {
            throw new RuntimeException("something went wrong");
        }
    }

    /**
     * 测试用 DTO
     */
    @Getter
    @Setter
    static class TestDTO {
        @NotBlank(message = "名称不能为空")
        private String name;

        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }

    @Test
    @DisplayName("BizException → 响应包含正确的 code 和 message")
    void handleBizException_shouldReturnCorrectCodeAndMessage() throws Exception {
        mockMvc.perform(post("/test/biz-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.MATERIAL_CODE_DUPLICATE.getCode()))
                .andExpect(jsonPath("$.message").value("物料编码已存在"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("BizException 自定义消息 → 响应包含自定义消息")
    void handleBizException_withCustomMessage_shouldReturnCustomMessage() throws Exception {
        mockMvc.perform(post("/test/biz-error-custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("物料xxx不存在"));
    }

    @Test
    @DisplayName("参数校验失败 → 响应包含 fieldErrors 列表")
    void handleValidationException_shouldReturnFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("参数校验失败 — 部分字段错误 → 返回对应字段的错误信息")
    void handleValidationException_singleField_shouldReturnSingleError() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试\",\"quantity\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.field=='quantity')].message").value(org.hamcrest.Matchers.hasItem("数量不能为空")));
    }

    @Test
    @DisplayName("未捕获异常 → 返回500 + 系统异常消息")
    void handleException_shouldReturnSystemError() throws Exception {
        mockMvc.perform(post("/test/system-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.SYSTEM_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("系统异常，请稍后重试"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
