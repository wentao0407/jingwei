-- ============================================================
-- V59: 恢复 admin 经营辅助通知、报表和成本入口权限种子数据
-- ============================================================
-- 说明：
-- 前端 Stage 8 继续接入通知中心、报表中心和成本核算入口。
-- 本迁移恢复对应菜单、按钮权限和 ADMIN 角色授权。

UPDATE t_sys_menu
SET deleted = FALSE, status = 'ACTIVE'
WHERE id IN (
    700, 710, 711, 712, 720, 721,
    800, 810, 811, 820, 821,
    900, 910, 911, 912, 920, 921, 922, 930, 931, 932, 940, 941, 942
);

INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status)
VALUES
    (700, 0, '通知中心', 'DIRECTORY', '/notification', '', '', 'BellOutlined', 7, TRUE, 'ACTIVE'),
    (710, 700, '我的通知', 'MENU', '/notification/list', 'notification/NotificationList', '', 'MailOutlined', 1, TRUE, 'ACTIVE'),
    (711, 710, '标记已读', 'BUTTON', '', '', 'notification:read', '', 1, TRUE, 'ACTIVE'),
    (712, 710, '全部已读', 'BUTTON', '', '', 'notification:readAll', '', 2, TRUE, 'ACTIVE'),
    (720, 700, '通知偏好', 'MENU', '/notification/preference', 'notification/NotificationPreference', '', 'SettingOutlined', 2, TRUE, 'ACTIVE'),
    (721, 720, '更新偏好', 'BUTTON', '', '', 'notification:pref:update', '', 1, TRUE, 'ACTIVE'),
    (800, 0, '成本核算', 'DIRECTORY', '/cost', '', '', 'DollarOutlined', 8, TRUE, 'ACTIVE'),
    (810, 800, '成本查询', 'MENU', '/cost/query', 'cost/CostQuery', '', 'DollarOutlined', 1, TRUE, 'ACTIVE'),
    (811, 810, '查询成本明细', 'BUTTON', '', '', 'cost:query:detail', '', 1, TRUE, 'ACTIVE'),
    (820, 800, '成本报表', 'MENU', '/cost/report', 'cost/CostReport', '', 'BarChartOutlined', 2, TRUE, 'ACTIVE'),
    (821, 820, '导出成本报表', 'BUTTON', '', '', 'cost:report:export', '', 1, TRUE, 'ACTIVE'),
    (900, 0, '报表中心', 'DIRECTORY', '/report', '', '', 'BarChartOutlined', 9, TRUE, 'ACTIVE'),
    (910, 900, '库存台账', 'MENU', '/report/ledger', 'report/InventoryLedger', '', 'BookOutlined', 1, TRUE, 'ACTIVE'),
    (911, 910, '查看库存台账', 'BUTTON', '', '', 'report:ledger:view', '', 1, TRUE, 'ACTIVE'),
    (912, 910, '导出库存台账', 'BUTTON', '', '', 'report:ledger:export', '', 2, TRUE, 'ACTIVE'),
    (920, 900, '出入库流水', 'MENU', '/report/flow', 'report/OperationFlow', '', 'FileTextOutlined', 2, TRUE, 'ACTIVE'),
    (921, 920, '查看出入库流水', 'BUTTON', '', '', 'report:flow:view', '', 1, TRUE, 'ACTIVE'),
    (922, 920, '导出出入库流水', 'BUTTON', '', '', 'report:flow:export', '', 2, TRUE, 'ACTIVE'),
    (930, 900, '库龄分析', 'MENU', '/report/age', 'report/InventoryAge', '', 'CalendarOutlined', 3, TRUE, 'ACTIVE'),
    (931, 930, '查看库龄分析', 'BUTTON', '', '', 'report:age:view', '', 1, TRUE, 'ACTIVE'),
    (932, 930, '导出库龄分析', 'BUTTON', '', '', 'report:age:export', '', 2, TRUE, 'ACTIVE'),
    (940, 900, '畅滞销分析', 'MENU', '/report/turnover', 'report/TurnoverAnalysis', '', 'BarChartOutlined', 4, TRUE, 'ACTIVE'),
    (941, 940, '查看畅滞销分析', 'BUTTON', '', '', 'report:turnover:view', '', 1, TRUE, 'ACTIVE'),
    (942, 940, '导出畅滞销分析', 'BUTTON', '', '', 'report:turnover:export', '', 2, TRUE, 'ACTIVE')
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id,
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    permission = EXCLUDED.permission,
    icon = EXCLUDED.icon,
    sort_order = EXCLUDED.sort_order,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    deleted = FALSE;

UPDATE t_sys_role_menu SET deleted = TRUE
WHERE id IN (
    SELECT id FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY role_id, menu_id ORDER BY id) AS rn
        FROM t_sys_role_menu
        WHERE menu_id IN (
            700, 710, 711, 712, 720, 721,
            800, 810, 811, 820, 821,
            900, 910, 911, 912, 920, 921, 922, 930, 931, 932, 940, 941, 942
        )
          AND deleted = FALSE
    ) t WHERE rn > 1
) AND deleted = FALSE;

INSERT INTO t_sys_role_menu (id, role_id, menu_id)
SELECT
    65000 + m.id,
    r.id,
    m.id
FROM t_sys_role r
JOIN t_sys_menu m ON m.id IN (
    700, 710, 711, 712, 720, 721,
    800, 810, 811, 820, 821,
    900, 910, 911, 912, 920, 921, 922, 930, 931, 932, 940, 941, 942
)
WHERE r.role_code = 'ADMIN'
  AND r.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1
      FROM t_sys_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );

UPDATE t_sys_role_menu
SET deleted = FALSE
WHERE role_id IN (
    SELECT id
    FROM t_sys_role
    WHERE role_code = 'ADMIN'
)
  AND menu_id IN (
      700, 710, 711, 712, 720, 721,
      800, 810, 811, 820, 821,
      900, 910, 911, 912, 920, 921, 922, 930, 931, 932, 940, 941, 942
  );
