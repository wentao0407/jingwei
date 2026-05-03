package com.jingwei.common.domain.model;

import lombok.Getter;

/**
 * 错误码枚举
 * <p>
 * 按模块分段编码：
 * <ul>
 *   <li>10xxx — 公共模块（参数校验、系统异常等）</li>
 *   <li>20xxx — 基础数据模块（物料、SKU、供应商等）</li>
 *   <li>30xxx — 订单管理模块（销售订单、生产订单等）</li>
 *   <li>40xxx — 采购管理模块（BOM、MRP、采购订单等）</li>
 *   <li>50xxx — 库存管理模块（四类库存、预留释放等）</li>
 *   <li>60xxx — 出入库作业模块（收货、拣货、发货等）</li>
 *   <li>70xxx — 审批引擎模块</li>
 *   <li>80xxx — 通知中心模块</li>
 *   <li>90xxx — 成本核算模块</li>
 *   <li>91xxx — 系统管理模块（用户、角色、权限等）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum ErrorCode {

    // ========== 公共模块 10xxx ==========
    /** 系统内部错误 */
    SYSTEM_ERROR(10001, "系统异常，请稍后重试"),
    /** 参数校验失败 */
    PARAM_VALIDATION_FAILED(10002, "参数校验失败"),
    /** 请求方法不允许 */
    METHOD_NOT_ALLOWED(10003, "请求方法不允许"),
    /** 请求资源不存在 */
    NOT_FOUND(10004, "请求资源不存在"),
    /** 未授权访问 */
    UNAUTHORIZED(10005, "未授权，请先登录"),
    /** 无权限 */
    FORBIDDEN(10006, "无权限访问该资源"),
    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(10007, "请求过于频繁，请稍后重试"),
    /** 数据已存在 */
    DATA_ALREADY_EXISTS(10008, "数据已存在"),
    /** 数据不存在 */
    DATA_NOT_FOUND(10009, "数据不存在"),
    /** 操作不允许 */
    OPERATION_NOT_ALLOWED(10010, "当前状态不允许此操作"),
    /** 并发冲突 */
    CONCURRENT_CONFLICT(10011, "数据已被他人修改，请刷新后重试"),

    // ========== 基础数据模块 20xxx ==========
    /** 物料编码重复 */
    MATERIAL_CODE_DUPLICATE(20001, "物料编码已存在"),
    /** 物料已被引用，不可删除 */
    MATERIAL_REFERENCED(20002, "物料已被引用，不可删除"),
    /** 分类编码同级重复 */
    CATEGORY_CODE_DUPLICATE(20003, "同级分类编码已存在"),
    /** 分类层级超限 */
    CATEGORY_LEVEL_EXCEEDED(20004, "分类层级不能超过3级"),
    /** 分类已被物料引用 */
    CATEGORY_REFERENCED_BY_MATERIAL(20005, "该分类已被物料引用，不可删除"),
    /** SKU编码重复 */
    SKU_CODE_DUPLICATE(20006, "SKU编码已存在"),
    /** 尺码组已被SPU引用 */
    SIZE_GROUP_REFERENCED(20007, "该尺码组已被款式引用，不可删除"),
    /** 编码规则流水号已用尽 */
    CODING_SEQUENCE_EXHAUSTED(20008, "编码规则流水号已用尽"),
    /** 编码规则已使用，不可删除 */
    CODING_RULE_USED(20009, "编码规则已使用，不可删除"),
    /** 供应商名称重复 */
    SUPPLIER_NAME_DUPLICATE(20010, "供应商名称已存在"),
    /** 供应商不合格 */
    SUPPLIER_DISQUALIFIED(20011, "供应商资质不合格，不可创建采购订单"),
    /** 客户名称重复 */
    CUSTOMER_NAME_DUPLICATE(20012, "客户名称已存在"),
    /** 库位已冻结 */
    LOCATION_FROZEN(20013, "库位已冻结，不可进行出入库操作"),
    /** 成分百分比合计不等于100% */
    COMPOSITION_PERCENT_INVALID(20014, "成分合计应为100%"),

    // ========== 订单管理模块 30xxx ==========
    /** 订单状态流转不合法 */
    ORDER_STATE_TRANSITION_INVALID(30001, "当前订单状态不允许此操作"),
    /** 订单行不能为空 */
    ORDER_LINE_EMPTY(30002, "订单至少需要一行明细"),
    /** 订单行重复 */
    ORDER_LINE_DUPLICATE(30003, "同一订单中不能添加重复的款式和颜色"),
    /** 订单已关联生产订单 */
    ORDER_LINKED_PRODUCTION(30004, "订单已关联生产订单"),
    /** 订单行信息不完整 */
    ORDER_LINE_INCOMPLETE(30005, "订单行信息不完整"),

    // ========== 采购管理模块 40xxx ==========
    /** BOM已引用，不可删除 */
    BOM_REFERENCED(40001, "BOM已被生产订单引用，不可删除"),
    /** BOM版本冲突 */
    BOM_VERSION_CONFLICT(40002, "BOM版本冲突"),
    /** 采购数量不足MOQ */
    PROCUREMENT_MOQ_NOT_MET(40003, "采购数量不足最小起订量"),

    // ========== 库存管理模块 50xxx ==========
    /** 库存不足 */
    INSUFFICIENT_INVENTORY(50001, "库存不足"),
    /** 库存预留过期 */
    ALLOCATION_EXPIRED(50002, "库存预留已过期"),
    /** 盘点进行中 */
    STOCKTAKING_IN_PROGRESS(50003, "该库位正在盘点，禁止出入库操作"),

    // ========== 出入库作业模块 60xxx ==========
    /** 波次未完成 */
    WAVE_NOT_COMPLETED(60001, "波次拣货未完成"),
    /** 拣货单已分配 */
    PICK_ALREADY_ASSIGNED(60002, "拣货单已分配"),

    // ========== 审批引擎模块 70xxx ==========
    /** 无审批配置 */
    APPROVAL_CONFIG_NOT_FOUND(70001, "未找到审批配置"),
    /** 无权审批 */
    APPROVAL_NO_PERMISSION(70002, "无权审批该单据"),
    /** 审批意见必填 */
    APPROVAL_COMMENT_REQUIRED(70003, "审批意见不能为空"),

    // ========== 通知中心模块 80xxx ==========
    /** 通知推送失败 */
    NOTIFICATION_PUSH_FAILED(80001, "通知推送失败"),

    // ========== 成本核算模块 90xxx ==========
    /** 成本计算异常 */
    COST_CALCULATION_ERROR(90001, "成本计算异常"),

    // ========== 系统管理模块 91xxx ==========
    /** 用户名已存在 */
    USERNAME_DUPLICATE(91001, "用户名已存在"),
    /** 用户名或密码错误 */
    LOGIN_FAILED(91002, "用户名或密码错误"),
    /** 用户已停用 */
    USER_INACTIVE(91003, "用户已停用，无法登录"),
    /** 角色已分配用户 */
    ROLE_ASSIGNED_TO_USER(91004, "角色已分配给用户，不可删除"),
    /** Token已过期 */
    TOKEN_EXPIRED(91005, "登录已过期，请重新登录"),
    /** Token无效 */
    TOKEN_INVALID(91006, "无效的登录凭证"),
    /** 菜单权限标识重复 */
    MENU_PERMISSION_DUPLICATE(91007, "菜单权限标识已存在"),
    /** 按钮类型菜单必须配置权限标识 */
    MENU_BUTTON_PERMISSION_REQUIRED(91008, "按钮类型菜单必须配置权限标识"),
    /** 菜单层级关系不合法 */
    MENU_HIERARCHY_INVALID(91009, "菜单层级关系不合法"),
    /** 菜单存在子菜单，不可删除 */
    MENU_HAS_CHILDREN(91010, "菜单存在子菜单，不可删除"),
    /** 菜单已分配给角色，不可删除 */
    MENU_ASSIGNED_TO_ROLE(91011, "菜单已分配给角色，不可删除"),
    /** 无权限访问 */
    ACCESS_DENIED(91012, "无权限访问该资源"),
    /** 密码已过期 */
    PASSWORD_EXPIRED(91013, "密码已过期，请修改密码"),
    /** 旧密码不正确 */
    OLD_PASSWORD_MISMATCH(91014, "旧密码不正确"),
    /** 新密码不能与旧密码相同 */
    PASSWORD_SAME_AS_OLD(91015, "新密码不能与旧密码相同");

    /** 错误码 */
    private final int code;

    /** 错误消息（中文） */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
