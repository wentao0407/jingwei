-- ============================================================
-- V37: 报表模块 — 菜单权限
-- ============================================================

-- 报表模块菜单（父级目录 900）
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    (900, 0,   '报表中心', 'DIRECTORY', '/report', '',                           '',                              'BarChartOutlined', 9, TRUE, 'ACTIVE'),
    (910, 900, '库存台账', 'MENU',      '/report/ledger', 'report/InventoryLedger', '',                        'BookOutlined', 1, TRUE, 'ACTIVE'),
    (920, 900, '出入库流水', 'MENU',    '/report/flow', 'report/OperationFlow', '',                            'SwapOutlined', 2, TRUE, 'ACTIVE'),
    (930, 900, '库龄分析', 'MENU',      '/report/age', 'report/InventoryAge', '',                              'ClockCircleOutlined', 3, TRUE, 'ACTIVE'),
    (940, 900, '畅滞销分析', 'MENU',    '/report/turnover', 'report/TurnoverAnalysis', '',                     'TrendingUpOutlined', 4, TRUE, 'ACTIVE');

-- 报表模块 → 按钮权限
INSERT INTO t_sys_menu (id, parent_id, name, type, path, component, permission, icon, sort_order, visible, status) VALUES
    (911, 910, '查看库存台账',   'BUTTON', '', '', 'report:ledger:view',    '', 1, TRUE, 'ACTIVE'),
    (912, 910, '导出库存台账',   'BUTTON', '', '', 'report:ledger:export',  '', 2, TRUE, 'ACTIVE'),
    (921, 920, '查看出入库流水', 'BUTTON', '', '', 'report:flow:view',      '', 1, TRUE, 'ACTIVE'),
    (922, 920, '导出出入库流水', 'BUTTON', '', '', 'report:flow:export',    '', 2, TRUE, 'ACTIVE'),
    (931, 930, '查看库龄分析',   'BUTTON', '', '', 'report:age:view',       '', 1, TRUE, 'ACTIVE'),
    (932, 930, '导出库龄分析',   'BUTTON', '', '', 'report:age:export',     '', 2, TRUE, 'ACTIVE'),
    (941, 940, '查看畅滞销分析', 'BUTTON', '', '', 'report:turnover:view',  '', 1, TRUE, 'ACTIVE'),
    (942, 940, '导出畅滞销分析', 'BUTTON', '', '', 'report:turnover:export','', 2, TRUE, 'ACTIVE');

-- 管理员角色(id=1)拥有所有报表权限
INSERT INTO t_sys_role_menu (id, role_id, menu_id) VALUES
    (20400, 1, 900),
    (20401, 1, 910),
    (20402, 1, 911),
    (20403, 1, 912),
    (20404, 1, 920),
    (20405, 1, 921),
    (20406, 1, 922),
    (20407, 1, 930),
    (20408, 1, 931),
    (20409, 1, 932),
    (20410, 1, 940),
    (20411, 1, 941),
    (20412, 1, 942)
ON CONFLICT (id) DO NOTHING;
