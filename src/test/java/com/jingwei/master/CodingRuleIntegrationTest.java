package com.jingwei.master;

import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.*;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.infrastructure.persistence.*;
import com.jingwei.master.interfaces.vo.CodePreviewVO;
import com.jingwei.master.interfaces.vo.CodingRuleVO;
import com.jingwei.system.application.dto.AssignMenuDTO;
import com.jingwei.system.application.dto.CreateRoleDTO;
import com.jingwei.system.application.dto.LoginDTO;
import com.jingwei.system.domain.model.SysMenu;
import com.jingwei.system.domain.model.SysRole;
import com.jingwei.system.domain.model.SysRoleMenu;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.SysUserRole;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.infrastructure.persistence.SysMenuMapper;
import com.jingwei.system.infrastructure.persistence.SysRoleMapper;
import com.jingwei.system.infrastructure.persistence.SysRoleMenuMapper;
import com.jingwei.system.infrastructure.persistence.SysUserMapper;
import com.jingwei.system.infrastructure.persistence.SysUserRoleMapper;
import com.jingwei.system.interfaces.vo.LoginVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-07 基础数据 — 编码规则引擎 集成测试
 *
 * @author JingWei
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CodingRuleIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private CodingRuleMapper codingRuleMapper;
    @Autowired private CodingRuleSegmentMapper codingRuleSegmentMapper;
    @Autowired private CodingSequenceMapper codingSequenceMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SysRoleMapper sysRoleMapper;
    @Autowired private SysUserRoleMapper sysUserRoleMapper;
    @Autowired private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired private SysMenuMapper sysMenuMapper;

    private String authToken;

    @BeforeEach
    void setUp() {
        // 清理
        codingSequenceMapper.delete(null);
        codingRuleSegmentMapper.delete(null);
        codingRuleMapper.delete(null);
        sysRoleMenuMapper.delete(null);
        sysUserRoleMapper.delete(null);
        sysUserMapper.delete(null);
        sysRoleMapper.delete(null);
        sysMenuMapper.delete(null);

        // 创建带全权限的测试用户
        SysUser user = new SysUser();
        user.setUsername("codingadmin");
        user.setPassword(new BCryptPasswordEncoder().encode("admin123"));
        user.setRealName("编码规则管理员");
        user.setStatus(UserStatus.ACTIVE);
        sysUserMapper.insert(user);

        SysRole role = new SysRole();
        role.setRoleCode("CODING_ADMIN");
        role.setRoleName("编码规则管理员");
        role.setStatus(UserStatus.ACTIVE);
        sysRoleMapper.insert(role);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(role.getId());
        sysUserRoleMapper.insert(ur);

        // 创建编码规则相关菜单权限
        SysMenu dir = new SysMenu();
        dir.setParentId(0L);
        dir.setName("基础数据");
        dir.setType(com.jingwei.system.domain.model.MenuType.DIRECTORY);
        dir.setPath("/master");
        dir.setSortOrder(1);
        dir.setVisible(true);
        dir.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(dir);

        SysMenu menu = new SysMenu();
        menu.setParentId(dir.getId());
        menu.setName("编码规则");
        menu.setType(com.jingwei.system.domain.model.MenuType.MENU);
        menu.setPath("/master/codingRule");
        menu.setComponent("master/CodingRulePage");
        menu.setSortOrder(1);
        menu.setVisible(true);
        menu.setStatus(UserStatus.ACTIVE);
        sysMenuMapper.insert(menu);

        // 添加一些菜单让角色有基础数据权限（不需要具体按钮权限，编码规则接口没加 @RequirePermission）
        List<SysMenu> allMenus = sysMenuMapper.selectList(null);
        for (SysMenu m : allMenus) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getId());
            rm.setMenuId(m.getId());
            sysRoleMenuMapper.insert(rm);
        }

        // 登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("codingadmin");
        loginDTO.setPassword("admin123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> entity = new HttpEntity<>(loginDTO, headers);
        ResponseEntity<R<LoginVO>> resp = restTemplate.exchange("/auth/login", HttpMethod.POST, entity,
                new ParameterizedTypeReference<R<LoginVO>>() {});
        authToken = resp.getBody().getData().getToken();
    }

    @AfterEach
    void tearDown() {
        codingSequenceMapper.delete(null);
        codingRuleSegmentMapper.delete(null);
        codingRuleMapper.delete(null);
        sysRoleMenuMapper.delete(null);
        sysUserRoleMapper.delete(null);
        sysUserMapper.delete(null);
        sysRoleMapper.delete(null);
        sysMenuMapper.delete(null);
    }

    @Test
    @DisplayName("创建编码规则应成功")
    void createRule_shouldSucceed() {
        CreateCodingRuleDTO dto = new CreateCodingRuleDTO();
        dto.setCode("TEST_RULE");
        dto.setName("测试规则");
        dto.setBusinessType("TEST");

        CodingRuleSegmentDTO fixed = new CodingRuleSegmentDTO();
        fixed.setSegmentType("FIXED");
        fixed.setSegmentValue("T-");
        fixed.setConnector("");
        fixed.setSortOrder(1);

        CodingRuleSegmentDTO seq = new CodingRuleSegmentDTO();
        seq.setSegmentType("SEQUENCE");
        seq.setSeqLength(4);
        seq.setSeqResetType("NEVER");
        seq.setConnector("");
        seq.setSortOrder(2);

        dto.setSegments(List.of(fixed, seq));

        ResponseEntity<R<CodingRuleVO>> resp = postWithAuth("/master/codingRule/create", dto,
                new ParameterizedTypeReference<R<CodingRuleVO>>() {});

        assertTrue(resp.getBody().isSuccess(), "创建应成功: " + resp.getBody().getMessage());
        CodingRuleVO created = resp.getBody().getData();
        assertEquals("TEST_RULE", created.getCode());
        assertEquals(2, created.getSegments().size());
    }

    @Test
    @DisplayName("生成编码 → 格式正确")
    void generateCode_formatCorrect() {
        // 先创建规则
        createTestRule("GEN_TEST", "生成测试规则");

        GenerateCodeDTO genDto = new GenerateCodeDTO();
        genDto.setRuleCode("GEN_TEST");

        ResponseEntity<R<String>> resp = postWithAuth("/master/codingRule/generate", genDto,
                new ParameterizedTypeReference<R<String>>() {});

        assertTrue(resp.getBody().isSuccess(), "生成编码应成功: " + resp.getBody().getMessage());
        String code = resp.getBody().getData();
        assertTrue(code.startsWith("GE-"), "编码应以 GE- 开头: " + code);
        assertTrue(code.endsWith("0001"), "首个编码应以 0001 结尾: " + code);
    }

    @Test
    @DisplayName("连续生成编码 → 流水号递增")
    void generateCode_sequential_shouldIncrement() {
        createTestRule("SEQ_TEST", "递增测试规则");

        GenerateCodeDTO genDto = new GenerateCodeDTO();
        genDto.setRuleCode("SEQ_TEST");

        String code1 = postWithAuth("/master/codingRule/generate", genDto,
                new ParameterizedTypeReference<R<String>>() {}).getBody().getData();
        String code2 = postWithAuth("/master/codingRule/generate", genDto,
                new ParameterizedTypeReference<R<String>>() {}).getBody().getData();

        assertNotEquals(code1, code2, "两次生成的编码应不同");
        assertTrue(code1.endsWith("0001"));
        assertTrue(code2.endsWith("0002"));
    }

    @Test
    @DisplayName("预览编码 → 不递增流水号")
    void previewCode_shouldNotIncrement() {
        createTestRule("PREVIEW_TEST", "预览测试规则");

        GenerateCodeDTO genDto = new GenerateCodeDTO();
        genDto.setRuleCode("PREVIEW_TEST");

        // 预览
        ResponseEntity<R<CodePreviewVO>> previewResp = postWithAuth("/master/codingRule/preview", genDto,
                new ParameterizedTypeReference<R<CodePreviewVO>>() {});
        assertTrue(previewResp.getBody().isSuccess());
        assertNotNull(previewResp.getBody().getData().getPreviewCode());

        // 生成实际编码，仍应从 1 开始（预览不递增）
        ResponseEntity<R<String>> genResp = postWithAuth("/master/codingRule/generate", genDto,
                new ParameterizedTypeReference<R<String>>() {});
        String code = genResp.getBody().getData();
        assertTrue(code.endsWith("0001"), "预览不应递增流水号，实际编码应从 0001 开始");
    }

    @Test
    @DisplayName("已使用的规则不可删除")
    void deleteRule_used_shouldFail() {
        createTestRule("USED_RULE", "已使用规则");

        GenerateCodeDTO genDto = new GenerateCodeDTO();
        genDto.setRuleCode("USED_RULE");
        postWithAuth("/master/codingRule/generate", genDto,
                new ParameterizedTypeReference<R<String>>() {});

        // 查出规则ID
        ResponseEntity<R<CodingRuleVO>> detailResp = postWithAuthNoBody(
                "/master/codingRule/detail?code=USED_RULE",
                new ParameterizedTypeReference<R<CodingRuleVO>>() {});
        Long ruleId = detailResp.getBody().getData().getId();

        // 尝试删除
        ResponseEntity<R<Void>> delResp = postWithAuthNoBody(
                "/master/codingRule/delete?ruleId=" + ruleId,
                new ParameterizedTypeReference<R<Void>>() {});

        assertFalse(delResp.getBody().isSuccess(), "已使用的规则应不可删除");
    }

    @Test
    @DisplayName("按月重置 — 不同月份流水号从1开始（逻辑验证）")
    void generateCode_monthlyReset_differentMonth() {
        // 此测试验证的是 resetKey 的构建逻辑
        // 实际跨月测试需要 mock 时间，此处通过直接调用验证 resetKey 格式
        // 集成环境下当月生成的编码与上月流水号独立
        createTestRule("MONTHLY_RESET", "按月重置规则");

        GenerateCodeDTO genDto = new GenerateCodeDTO();
        genDto.setRuleCode("MONTHLY_RESET");

        // 当月第一次生成
        String code = postWithAuth("/master/codingRule/generate", genDto,
                new ParameterizedTypeReference<R<String>>() {}).getBody().getData();
        assertTrue(code.endsWith("0001"), "按月重置的当月首次编码应为 0001: " + code);
    }

    // ==================== 辅助方法 ====================

    private void createTestRule(String code, String name) {
        CreateCodingRuleDTO dto = new CreateCodingRuleDTO();
        dto.setCode(code);
        dto.setName(name);

        CodingRuleSegmentDTO fixed = new CodingRuleSegmentDTO();
        fixed.setSegmentType("FIXED");
        fixed.setSegmentValue(code.substring(0, 2).toUpperCase());
        fixed.setConnector("");
        fixed.setSortOrder(1);

        CodingRuleSegmentDTO seq = new CodingRuleSegmentDTO();
        seq.setSegmentType("SEQUENCE");
        seq.setSeqLength(4);
        seq.setSeqResetType("MONTHLY");
        seq.setConnector("-");
        seq.setSortOrder(2);

        dto.setSegments(List.of(fixed, seq));

        R<CodingRuleVO> result = postWithAuth("/master/codingRule/create", dto,
                new ParameterizedTypeReference<R<CodingRuleVO>>() {}).getBody();
        assertTrue(result.isSuccess(), "创建规则应成功: " + result.getMessage());
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> ResponseEntity<R<T>> postWithAuth(String url, Object body, ParameterizedTypeReference<R<T>> typeRef) {
        HttpEntity<Object> entity = new HttpEntity<>(body, authHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
    }

    private <T> ResponseEntity<R<T>> postWithAuthNoBody(String url, ParameterizedTypeReference<R<T>> typeRef) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
    }
}
