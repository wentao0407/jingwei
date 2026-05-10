-- ============================================================
-- V54: Demo data for current visible menus
-- Purpose:
--   Keep every current menu-backed list usable with at least 30 demo rows.
--   IDs are placed in the 900000+ range to avoid colliding with base seeds.
--   Data is intentionally connected across modules:
--   master data -> sales order -> production/return -> BOM/MRP/procurement ->
--   inventory/warehouse operations.
-- ============================================================

-- ----------------------------
-- System management data
-- ----------------------------
INSERT INTO t_sys_role (id, role_code, role_name, description, status)
SELECT
    900000 + i,
    'DEMO_ROLE_' || LPAD(i::text, 2, '0'),
    '演示角色' || LPAD(i::text, 2, '0'),
    '用于菜单数据验收的演示角色',
    CASE WHEN i % 10 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_user (id, username, password, real_name, phone, email, status, password_updated_at)
SELECT
    901000 + i,
    'demo_user_' || LPAD(i::text, 2, '0'),
    'DEMO_DISABLED_LOGIN_NOT_ALLOWED',
    '演示用户' || LPAD(i::text, 2, '0'),
    '1390000' || LPAD(i::text, 4, '0'),
    'demo.user' || LPAD(i::text, 2, '0') || '@jingwei.local',
    'INACTIVE',
    NOW()
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_user_role (id, user_id, role_id)
SELECT
    902000 + i,
    901000 + i,
    900000 + i
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_config (id, config_key, config_value, config_group, description, need_restart, remark)
SELECT
    903000 + i,
    'demo.menu.seed.config.' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 2 = 0 THEN 'true' ELSE (100 + i)::text END,
    CASE (i % 5)
        WHEN 0 THEN 'INVENTORY'
        WHEN 1 THEN 'PASSWORD'
        WHEN 2 THEN 'MRP'
        WHEN 3 THEN 'ORDER'
        ELSE 'DEFAULT'
    END,
    '演示配置项 ' || LPAD(i::text, 2, '0'),
    i % 6 = 0,
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_data_scope (id, role_id, scope_type, scope_value)
SELECT
    904000 + i,
    900000 + i,
    CASE WHEN i % 3 = 0 THEN 'WAREHOUSE' WHEN i % 3 = 1 THEN 'DEPT' ELSE 'ALL' END,
    CASE WHEN i % 3 = 2 THEN 'ALL' ELSE (940000 + i)::text END
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_sys_audit_log (id, user_id, username, operation_type, module, description, old_value, new_value, ip_address, created_at)
SELECT
    905000 + i,
    901000 + i,
    'demo_user_' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 4 = 0 THEN 'UPDATE' WHEN i % 4 = 1 THEN 'CREATE' WHEN i % 4 = 2 THEN 'LOGIN' ELSE 'OTHER' END,
    CASE WHEN i % 3 = 0 THEN 'MASTER' WHEN i % 3 = 1 THEN 'ORDER' ELSE 'SYSTEM' END,
    '演示审计日志 ' || LPAD(i::text, 2, '0'),
    '{"source":"seed"}',
    '{"status":"ok"}',
    '127.0.0.1',
    NOW() - (i || ' hours')::interval
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

-- ----------------------------
-- Master data
-- ----------------------------
INSERT INTO t_md_category (id, parent_id, code, name, level, sort_order, status)
SELECT
    910000 + i,
    CASE WHEN i <= 10 THEN 1002 WHEN i <= 20 THEN 1003 ELSE 1001 END,
    'DEMO_CAT_' || LPAD(i::text, 2, '0'),
    '演示分类' || LPAD(i::text, 2, '0'),
    2,
    100 + i,
    'ACTIVE'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_customer (
    id, code, name, short_name, type, level, contact_person, contact_phone,
    address, delivery_address, settlement_type, credit_limit, status, remark
)
SELECT
    911000 + i,
    'CUS-DEMO-' || LPAD(i::text, 3, '0'),
    '演示客户' || LPAD(i::text, 2, '0'),
    '演客' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 4 = 0 THEN 'ONLINE' WHEN i % 4 = 1 THEN 'WHOLESALE' WHEN i % 4 = 2 THEN 'RETAIL' ELSE 'FRANCHISE' END,
    CASE WHEN i % 4 = 0 THEN 'A' WHEN i % 4 = 1 THEN 'B' WHEN i % 4 = 2 THEN 'C' ELSE 'D' END,
    '客户联系人' || LPAD(i::text, 2, '0'),
    '1380000' || LPAD(i::text, 4, '0'),
    '上海市演示路' || i || '号',
    '上海市演示仓收货点' || i,
    CASE WHEN i % 3 = 0 THEN 'MONTHLY' WHEN i % 3 = 1 THEN 'QUARTERLY' ELSE 'COD' END,
    50000 + i * 1000,
    CASE WHEN i % 12 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_supplier (
    id, code, name, short_name, type, contact_person, contact_phone,
    address, settlement_type, lead_time_days, qualification_status, status, remark
)
SELECT
    912000 + i,
    'SUP-DEMO-' || LPAD(i::text, 3, '0'),
    '演示供应商' || LPAD(i::text, 2, '0'),
    '演供' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 4 = 0 THEN 'FABRIC' WHEN i % 4 = 1 THEN 'TRIM' WHEN i % 4 = 2 THEN 'PACKAGING' ELSE 'COMPOSITE' END,
    '供应联系人' || LPAD(i::text, 2, '0'),
    '1370000' || LPAD(i::text, 4, '0'),
    '浙江省演示供应链园区' || i || '号',
    CASE WHEN i % 3 = 0 THEN 'MONTHLY' WHEN i % 3 = 1 THEN 'QUARTERLY' ELSE 'COD' END,
    7 + (i % 20),
    CASE WHEN i % 10 = 0 THEN 'PENDING' ELSE 'QUALIFIED' END,
    CASE WHEN i % 13 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_material (id, code, name, type, category_id, unit, status, ext_attrs, remark)
SELECT
    913000 + i,
    'MAT-DEMO-' || LPAD(i::text, 3, '0'),
    '演示物料' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 3 = 0 THEN 'FABRIC' WHEN i % 3 = 1 THEN 'TRIM' ELSE 'PACKAGING' END,
    910000 + i,
    CASE WHEN i % 3 = 0 THEN '米' WHEN i % 3 = 1 THEN '个' ELSE '套' END,
    CASE WHEN i % 14 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    jsonb_build_object('seedNo', i, 'source', 'V54'),
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_size_group (id, code, name, category, status)
SELECT
    914000 + i,
    'DEMO_SIZE_' || LPAD(i::text, 2, '0'),
    '演示尺码组' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 3 = 0 THEN 'WOMEN' WHEN i % 3 = 1 THEN 'MEN' ELSE 'CHILDREN' END,
    'ACTIVE'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_size (id, size_group_id, code, name, sort_order)
SELECT
    9140000 + g.i * 10 + s.sort_order,
    914000 + g.i,
    s.code,
    s.code,
    s.sort_order
FROM generate_series(1, 30) AS g(i)
CROSS JOIN (VALUES (1, 'XS'), (2, 'S'), (3, 'M'), (4, 'L'), (5, 'XL')) AS s(sort_order, code)
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_season (id, code, name, year, season_type, start_date, end_date, status)
SELECT
    915000 + i,
    (2027 + ((i - 1) / 2))::text || CASE WHEN i % 2 = 1 THEN 'SS-DEMO' ELSE 'AW-DEMO' END,
    (2027 + ((i - 1) / 2))::text || CASE WHEN i % 2 = 1 THEN '春夏演示' ELSE '秋冬演示' END,
    2027 + ((i - 1) / 2),
    CASE WHEN i % 2 = 1 THEN 'SPRING_SUMMER' ELSE 'AUTUMN_WINTER' END,
    CASE WHEN i % 2 = 1 THEN make_date(2027 + ((i - 1) / 2), 1, 1) ELSE make_date(2027 + ((i - 1) / 2), 7, 1) END,
    CASE WHEN i % 2 = 1 THEN make_date(2027 + ((i - 1) / 2), 6, 30) ELSE make_date(2027 + ((i - 1) / 2), 12, 31) END,
    'ACTIVE'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_wave (id, season_id, code, name, delivery_date, sort_order)
SELECT
    915100 + i,
    915000 + i,
    'DEMO-W' || LPAD(i::text, 2, '0'),
    '演示波段' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 2 = 1 THEN make_date(2027 + ((i - 1) / 2), 4, 15) ELSE make_date(2027 + ((i - 1) / 2), 10, 15) END,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_spu (id, code, name, season_id, category_id, size_group_id, status, remark)
SELECT
    916000 + i,
    'SP-DEMO-' || LPAD(i::text, 3, '0'),
    '演示款式' || LPAD(i::text, 2, '0'),
    915000 + i,
    910000 + i,
    914000 + i,
    'ACTIVE',
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_color_way (id, spu_id, color_name, color_code, pantone_code, fabric_material_id, sort_order)
SELECT
    916100 + i,
    916000 + i,
    CASE WHEN i % 3 = 0 THEN '黑色' WHEN i % 3 = 1 THEN '米白' ELSE '海军蓝' END,
    'C' || LPAD(i::text, 2, '0'),
    'P' || LPAD((100 + i)::text, 3, '0'),
    913000 + i,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_sku (id, code, barcode, spu_id, color_way_id, size_id, cost_price, sale_price, wholesale_price, status)
SELECT
    916200 + i,
    'SKU-DEMO-' || LPAD(i::text, 3, '0'),
    '690000' || LPAD(i::text, 7, '0'),
    916000 + i,
    916100 + i,
    9140000 + i * 10 + 3,
    80 + i,
    199 + i,
    139 + i,
    'ACTIVE'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_warehouse (id, code, name, type, address, manager_id, status, remark)
SELECT
    917000 + i,
    'DWH' || LPAD(i::text, 2, '0'),
    '演示仓库' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 3 = 0 THEN 'FINISHED_GOODS' WHEN i % 3 = 1 THEN 'RAW_MATERIAL' ELSE 'RETURN' END,
    '江苏省演示物流园' || i || '号',
    901000 + i,
    CASE WHEN i % 11 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_location (
    id, warehouse_id, zone_code, rack_code, row_code, bin_code, full_code,
    location_type, capacity, used_capacity, status, remark
)
SELECT
    917100 + i,
    917000 + i,
    'A',
    LPAD(i::text, 2, '0'),
    '01',
    '01',
    'DWH' || LPAD(i::text, 2, '0') || '-A-' || LPAD(i::text, 2, '0') || '-01-01',
    CASE WHEN i % 4 = 0 THEN 'PICKING' WHEN i % 4 = 1 THEN 'STORAGE' WHEN i % 4 = 2 THEN 'STAGING' ELSE 'QC' END,
    1000 + i * 10,
    i * 3,
    'ACTIVE',
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule (id, code, name, business_type, description, status, used)
SELECT
    918000 + i,
    'DEMO_RULE_' || LPAD(i::text, 2, '0'),
    '演示编码规则' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 4 = 0 THEN 'ORDER' WHEN i % 4 = 1 THEN 'MASTER' WHEN i % 4 = 2 THEN 'WAREHOUSE' ELSE 'PROCUREMENT' END,
    'V54 demo seed',
    'ACTIVE',
    FALSE
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order)
SELECT 918100 + i, 918000 + i, 'FIXED', 'DM' || LPAD(i::text, 2, '0'), 0, 'NEVER', '', 1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order)
SELECT 918200 + i, 918000 + i, 'DATE', 'YYYYMMDD', 0, 'NEVER', '-', 2
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_md_coding_rule_segment (id, rule_id, segment_type, segment_value, seq_length, seq_reset_type, connector, sort_order)
SELECT 918300 + i, 918000 + i, 'SEQUENCE', '', 4, 'DAILY', '-', 3
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

-- ----------------------------
-- Order data
-- ----------------------------
INSERT INTO t_order_sales (
    id, order_no, customer_id, season_id, order_date, delivery_date, status,
    total_quantity, total_amount, discount_amount, actual_amount, payment_status,
    payment_amount, sales_rep_id, remark, created_by, updated_by
)
SELECT
    920000 + i,
    'SO-DEMO-202605-' || LPAD(i::text, 4, '0'),
    911000 + i,
    915000 + i,
    DATE '2026-05-01' + i,
    DATE '2026-06-01' + i,
    CASE WHEN i % 6 = 0 THEN 'DRAFT' WHEN i % 6 = 1 THEN 'PENDING_APPROVAL' WHEN i % 6 = 2 THEN 'CONFIRMED' WHEN i % 6 = 3 THEN 'PRODUCING' WHEN i % 6 = 4 THEN 'READY' ELSE 'COMPLETED' END,
    30 + i,
    (30 + i) * (100 + i),
    ROUND(((30 + i) * (100 + i) * 0.05)::numeric, 2),
    ROUND(((30 + i) * (100 + i) * 0.95)::numeric, 2),
    CASE WHEN i % 3 = 0 THEN 'PAID' WHEN i % 3 = 1 THEN 'UNPAID' ELSE 'PARTIAL' END,
    CASE WHEN i % 3 = 0 THEN ROUND(((30 + i) * (100 + i) * 0.95)::numeric, 2) WHEN i % 3 = 2 THEN 1000 ELSE 0 END,
    901000 + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_order_sales_line (
    id, order_id, line_no, spu_id, color_way_id, size_matrix, total_quantity,
    unit_price, line_amount, discount_rate, discount_amount, actual_amount,
    delivery_date, remark, created_by, updated_by
)
SELECT
    920100 + i,
    920000 + i,
    1,
    916000 + i,
    916100 + i,
    jsonb_build_object(
        'sizeGroupId', (914000 + i)::text,
        'sizes', jsonb_build_array(
            jsonb_build_object('sizeId', (9140000 + i * 10 + 2)::text, 'code', 'S', 'quantity', 10 + i),
            jsonb_build_object('sizeId', (9140000 + i * 10 + 3)::text, 'code', 'M', 'quantity', 20)
        )
    ),
    30 + i,
    100 + i,
    (30 + i) * (100 + i),
    0.9500,
    ROUND(((30 + i) * (100 + i) * 0.05)::numeric, 2),
    ROUND(((30 + i) * (100 + i) * 0.95)::numeric, 2),
    DATE '2026-06-01' + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_order_quantity_change (
    id, order_id, order_line_id, size_matrix_before, size_matrix_after,
    diff_matrix, reason, status, approved_by, approved_at, created_by, updated_by, updated_at
)
SELECT
    920200 + i,
    920000 + i,
    920100 + i,
    jsonb_build_object('S', 10 + i, 'M', 20),
    jsonb_build_object('S', 11 + i, 'M', 22),
    jsonb_build_object('S', 1, 'M', 2),
    '客户追加演示数量',
    CASE WHEN i % 3 = 0 THEN 'APPROVED' WHEN i % 3 = 1 THEN 'PENDING' ELSE 'REJECTED' END,
    CASE WHEN i % 3 = 0 THEN 1 ELSE NULL END,
    CASE WHEN i % 3 = 0 THEN NOW() ELSE NULL END,
    1,
    1,
    NOW()
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_order_production (
    id, order_no, plan_date, deadline_date, status, source_type, workshop_id,
    total_quantity, completed_quantity, stocked_quantity, remark, created_by, updated_by
)
SELECT
    921000 + i,
    'MO-DEMO-202605-' || LPAD(i::text, 4, '0'),
    DATE '2026-05-10' + i,
    DATE '2026-06-20' + i,
    CASE WHEN i % 5 = 0 THEN 'DRAFT' WHEN i % 5 = 1 THEN 'RELEASED' WHEN i % 5 = 2 THEN 'CUTTING' WHEN i % 5 = 3 THEN 'SEWING' ELSE 'COMPLETED' END,
    'SALES_ORDER',
    917000 + i,
    30 + i,
    CASE WHEN i % 5 = 4 THEN 30 + i ELSE i END,
    CASE WHEN i % 5 = 4 THEN 30 + i ELSE 0 END,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_order_production_line (
    id, order_id, line_no, spu_id, color_way_id, size_matrix, total_quantity,
    completed_quantity, stocked_quantity, skip_cutting, status, remark, created_by, updated_by
)
SELECT
    921100 + i,
    921000 + i,
    1,
    916000 + i,
    916100 + i,
    jsonb_build_object('sizeGroupId', (914000 + i)::text, 'sizes', jsonb_build_array(jsonb_build_object('sizeId', (9140000 + i * 10 + 3)::text, 'code', 'M', 'quantity', 30 + i))),
    30 + i,
    CASE WHEN i % 5 = 4 THEN 30 + i ELSE i END,
    CASE WHEN i % 5 = 4 THEN 30 + i ELSE 0 END,
    i % 4 = 0,
    CASE WHEN i % 5 = 0 THEN 'DRAFT' WHEN i % 5 = 1 THEN 'RELEASED' WHEN i % 5 = 2 THEN 'CUTTING' WHEN i % 5 = 3 THEN 'SEWING' ELSE 'COMPLETED' END,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_order_production_source (id, production_order_id, production_line_id, sales_order_id, sales_line_id, allocated_quantity)
SELECT
    921200 + i,
    921000 + i,
    921100 + i,
    920000 + i,
    920100 + i,
    30 + i
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_order_return (
    id, return_no, return_type, sales_order_id, sales_order_no, customer_id,
    reason, status, total_quantity, approved_by, approved_at, remark
)
SELECT
    922000 + i,
    'RT-DEMO-202605-' || LPAD(i::text, 4, '0'),
    CASE WHEN i % 3 = 0 THEN 'CUSTOMER_REJECT' WHEN i % 3 = 1 THEN 'LOGISTICS_REJECT' ELSE 'DISTRIBUTOR_RETURN' END,
    920000 + i,
    'SO-DEMO-202605-' || LPAD(i::text, 4, '0'),
    911000 + i,
    '演示退货原因',
    CASE WHEN i % 4 = 0 THEN 'DRAFT' WHEN i % 4 = 1 THEN 'PENDING_APPROVAL' WHEN i % 4 = 2 THEN 'APPROVED' ELSE 'COMPLETED' END,
    2 + (i % 5),
    CASE WHEN i % 4 IN (2, 3) THEN 1 ELSE NULL END,
    CASE WHEN i % 4 IN (2, 3) THEN NOW() ELSE NULL END,
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_order_return_line (
    id, return_id, sales_order_line_id, spu_id, color_way_id, size_matrix,
    total_quantity, qc_passed_qty, qc_failed_qty, qc_result, remark
)
SELECT
    922100 + i,
    922000 + i,
    920100 + i,
    916000 + i,
    916100 + i,
    jsonb_build_object('sizeGroupId', (914000 + i)::text, 'sizes', jsonb_build_array(jsonb_build_object('sizeId', (9140000 + i * 10 + 3)::text, 'code', 'M', 'quantity', 2 + (i % 5)))),
    2 + (i % 5),
    1 + (i % 3),
    1,
    jsonb_build_object('checked', true, 'seedNo', i),
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

-- ----------------------------
-- Procurement data
-- ----------------------------
INSERT INTO t_bom (id, code, spu_id, bom_version, status, effective_from, remark, created_by, updated_by)
SELECT
    923000 + i,
    'BOM-DEMO-' || LPAD(i::text, 4, '0'),
    916000 + i,
    1,
    'DRAFT',
    DATE '2026-05-01',
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_bom_item (
    id, bom_id, material_id, material_type, consumption_type, base_consumption,
    base_size_id, unit, wastage_rate, size_consumptions, sort_order, remark, created_by, updated_by
)
SELECT
    923100 + i,
    923000 + i,
    913000 + i,
    CASE WHEN i % 3 = 0 THEN 'FABRIC' WHEN i % 3 = 1 THEN 'TRIM' ELSE 'PACKAGING' END,
    CASE WHEN i % 2 = 0 THEN 'SIZE_DEPENDENT' ELSE 'FIXED_PER_PIECE' END,
    ROUND((1.2 + i * 0.03)::numeric, 4),
    9140000 + i * 10 + 3,
    CASE WHEN i % 3 = 0 THEN '米' WHEN i % 3 = 1 THEN '个' ELSE '套' END,
    0.0500,
    jsonb_build_object('M', ROUND((1.2 + i * 0.03)::numeric, 4)),
    1,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_procurement_mrp_result (
    id, batch_no, material_id, material_type, gross_demand, allocated_stock,
    in_transit_quantity, net_demand, suggested_quantity, unit, suggested_supplier_id,
    estimated_cost, earliest_delivery_date, status, snapshot_time, remark, created_by, updated_by
)
SELECT
    924000 + i,
    'MRP-DEMO-202605-' || LPAD(i::text, 3, '0'),
    913000 + i,
    CASE WHEN i % 3 = 0 THEN 'FABRIC' WHEN i % 3 = 1 THEN 'TRIM' ELSE 'PACKAGING' END,
    100 + i,
    10,
    5,
    85 + i,
    100 + i,
    CASE WHEN i % 3 = 0 THEN '米' WHEN i % 3 = 1 THEN '个' ELSE '套' END,
    912000 + i,
    (100 + i) * (20 + i),
    DATE '2026-05-20' + i,
    CASE WHEN i % 4 = 0 THEN 'PENDING' WHEN i % 4 = 1 THEN 'APPROVED' WHEN i % 4 = 2 THEN 'CONVERTED' ELSE 'IGNORED' END,
    NOW(),
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_procurement_mrp_source (
    id, batch_no, result_id, production_order_id, production_line_id, spu_id,
    color_way_id, material_id, bom_id, demand_quantity, detail
)
SELECT
    924100 + i,
    'MRP-DEMO-202605-' || LPAD(i::text, 3, '0'),
    924000 + i,
    921000 + i,
    921100 + i,
    916000 + i,
    916100 + i,
    913000 + i,
    923000 + i,
    85 + i,
    jsonb_build_object('seedNo', i)
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_procurement_order (
    id, order_no, supplier_id, order_date, expected_delivery_date, status,
    total_amount, paid_amount, payment_status, mrp_batch_no, remark, created_by, updated_by
)
SELECT
    925000 + i,
    'PO-DEMO-202605-' || LPAD(i::text, 4, '0'),
    912000 + i,
    DATE '2026-05-01' + i,
    DATE '2026-05-20' + i,
    CASE WHEN i % 5 = 0 THEN 'DRAFT' WHEN i % 5 = 1 THEN 'APPROVED' WHEN i % 5 = 2 THEN 'ISSUED' WHEN i % 5 = 3 THEN 'RECEIVING' ELSE 'COMPLETED' END,
    (100 + i) * (20 + i),
    CASE WHEN i % 3 = 0 THEN (100 + i) * (20 + i) ELSE 0 END,
    CASE WHEN i % 3 = 0 THEN 'PAID' WHEN i % 3 = 1 THEN 'UNPAID' ELSE 'PARTIAL' END,
    'MRP-DEMO-202605-' || LPAD(i::text, 3, '0'),
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_procurement_order_line (
    id, order_id, line_no, material_id, material_type, quantity, unit,
    unit_price, line_amount, delivered_quantity, accepted_quantity,
    rejected_quantity, mrp_result_id, remark, created_by, updated_by
)
SELECT
    925100 + i,
    925000 + i,
    1,
    913000 + i,
    CASE WHEN i % 3 = 0 THEN 'FABRIC' WHEN i % 3 = 1 THEN 'TRIM' ELSE 'PACKAGING' END,
    100 + i,
    CASE WHEN i % 3 = 0 THEN '米' WHEN i % 3 = 1 THEN '个' ELSE '套' END,
    20 + i,
    (100 + i) * (20 + i),
    CASE WHEN i % 5 = 4 THEN 100 + i ELSE 20 + i END,
    CASE WHEN i % 5 = 4 THEN 100 + i ELSE 15 + i END,
    0,
    924000 + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_procurement_asn (
    id, asn_no, procurement_order_id, supplier_id, expected_arrival_date,
    actual_arrival_date, status, receiver_id, remark, created_by, updated_by
)
SELECT
    926000 + i,
    'ASN-DEMO-202605-' || LPAD(i::text, 4, '0'),
    925000 + i,
    912000 + i,
    DATE '2026-05-20' + i,
    CASE WHEN i % 3 = 0 THEN DATE '2026-05-21' + i ELSE NULL END,
    CASE WHEN i % 3 = 0 THEN 'RECEIVED' WHEN i % 3 = 1 THEN 'PENDING' ELSE 'PARTIAL_RECEIVED' END,
    901000 + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_procurement_asn_line (
    id, asn_id, procurement_line_id, material_id, expected_quantity,
    received_quantity, qc_status, accepted_quantity, rejected_quantity,
    batch_no, qc_result, remark, created_by, updated_by
)
SELECT
    926100 + i,
    926000 + i,
    925100 + i,
    913000 + i,
    100 + i,
    CASE WHEN i % 3 = 0 THEN 100 + i ELSE 0 END,
    CASE WHEN i % 3 = 0 THEN 'PASSED' WHEN i % 3 = 1 THEN 'PENDING' ELSE 'CONCESSION' END,
    CASE WHEN i % 3 = 0 THEN 100 + i ELSE 0 END,
    0,
    'BATCH-DEMO-' || LPAD(i::text, 3, '0'),
    jsonb_build_object('seedNo', i),
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

-- ----------------------------
-- Inventory and warehouse data
-- ----------------------------
INSERT INTO t_inventory_sku (
    id, sku_id, warehouse_id, location_id, batch_no, available_qty, locked_qty,
    qc_qty, total_qty, in_transit_qty, unit_cost, last_inbound_date, last_outbound_date,
    created_by, updated_by
)
SELECT
    927000 + i,
    916200 + i,
    917000 + i,
    917100 + i,
    'FG-DEMO-' || LPAD(i::text, 3, '0'),
    100 + i,
    5,
    0,
    105 + i,
    0,
    80 + i,
    DATE '2026-05-01' + i,
    DATE '2026-05-10' + i,
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT DO NOTHING;

INSERT INTO t_inventory_material (
    id, material_id, warehouse_id, location_id, batch_no, supplier_id,
    procurement_order_id, available_qty, locked_qty, qc_qty, total_qty,
    in_transit_qty, unit_cost, roll_count, last_inbound_date, created_by, updated_by
)
SELECT
    927100 + i,
    913000 + i,
    917000 + i,
    917100 + i,
    'RM-DEMO-' || LPAD(i::text, 3, '0'),
    912000 + i,
    925000 + i,
    500 + i,
    20,
    0,
    520 + i,
    0,
    20 + i,
    5 + (i % 6),
    DATE '2026-05-01' + i,
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT DO NOTHING;

INSERT INTO t_inventory_in_transit (
    id, procurement_order_id, procurement_line_id, material_id, warehouse_id,
    expected_qty, received_qty, remaining_qty, expected_arrival_date, status,
    created_by, updated_by
)
SELECT
    927200 + i,
    925000 + i,
    925100 + i,
    913000 + i,
    917000 + i,
    100 + i,
    CASE WHEN i % 3 = 0 THEN 100 + i ELSE 20 END,
    CASE WHEN i % 3 = 0 THEN 0 ELSE 80 + i END,
    DATE '2026-05-20' + i,
    CASE WHEN i % 3 = 0 THEN 'FULLY_RECEIVED' WHEN i % 3 = 1 THEN 'PENDING' ELSE 'PARTIAL_RECEIVED' END,
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_inventory_operation (
    id, operation_no, operation_type, inventory_type, inventory_id, sku_id,
    material_id, warehouse_id, location_id, batch_no, quantity,
    available_before, available_after, total_before, total_after,
    source_type, source_id, source_no, unit_cost, cost_amount, operator_id, remark
)
SELECT
    927300 + i,
    'IOP-DEMO-202605-' || LPAD(i::text, 4, '0'),
    CASE WHEN i % 2 = 0 THEN 'INBOUND_PURCHASE' ELSE 'OUTBOUND_SALES' END,
    CASE WHEN i % 2 = 0 THEN 'MATERIAL' ELSE 'SKU' END,
    CASE WHEN i % 2 = 0 THEN 927100 + i ELSE 927000 + i END,
    CASE WHEN i % 2 = 0 THEN NULL ELSE 916200 + i END,
    CASE WHEN i % 2 = 0 THEN 913000 + i ELSE NULL END,
    917000 + i,
    917100 + i,
    CASE WHEN i % 2 = 0 THEN 'RM-DEMO-' || LPAD(i::text, 3, '0') ELSE 'FG-DEMO-' || LPAD(i::text, 3, '0') END,
    10 + i,
    100,
    110 + i,
    100,
    110 + i,
    CASE WHEN i % 2 = 0 THEN 'PROCUREMENT_ORDER' ELSE 'SALES_ORDER' END,
    CASE WHEN i % 2 = 0 THEN 925000 + i ELSE 920000 + i END,
    CASE WHEN i % 2 = 0 THEN 'PO-DEMO-202605-' || LPAD(i::text, 4, '0') ELSE 'SO-DEMO-202605-' || LPAD(i::text, 4, '0') END,
    20 + i,
    (10 + i) * (20 + i),
    901000 + i,
    'V54 demo seed'
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_inventory_allocation (
    id, allocation_no, order_type, order_id, order_line_id, sku_id,
    warehouse_id, batch_no, allocated_qty, fulfilled_qty, remaining_qty,
    status, expire_at, remark, created_by, updated_by
)
SELECT
    927400 + i,
    'ALLOC-DEMO-202605-' || LPAD(i::text, 4, '0'),
    'SALES',
    920000 + i,
    920100 + i,
    916200 + i,
    917000 + i,
    'FG-DEMO-' || LPAD(i::text, 3, '0'),
    10 + i,
    CASE WHEN i % 3 = 0 THEN 10 + i ELSE 0 END,
    CASE WHEN i % 3 = 0 THEN 0 ELSE 10 + i END,
    CASE WHEN i % 3 = 0 THEN 'FULFILLED' WHEN i % 3 = 1 THEN 'ACTIVE' ELSE 'RELEASED' END,
    NOW() + INTERVAL '7 days',
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_inbound (
    id, inbound_no, inbound_type, warehouse_id, status, source_type,
    source_id, source_no, operator_id, inbound_date, remark, created_by, updated_by
)
SELECT
    928000 + i,
    'IN-DEMO-202605-' || LPAD(i::text, 4, '0'),
    CASE WHEN i % 3 = 0 THEN 'PURCHASE' WHEN i % 3 = 1 THEN 'PRODUCTION' ELSE 'RETURN_SALES' END,
    917000 + i,
    CASE WHEN i % 3 = 0 THEN 'COMPLETED' WHEN i % 3 = 1 THEN 'CONFIRMED' ELSE 'DRAFT' END,
    CASE WHEN i % 3 = 0 THEN 'PROCUREMENT_ORDER' WHEN i % 3 = 1 THEN 'PRODUCTION_ORDER' ELSE 'SALES_ORDER' END,
    CASE WHEN i % 3 = 0 THEN 925000 + i WHEN i % 3 = 1 THEN 921000 + i ELSE 920000 + i END,
    CASE WHEN i % 3 = 0 THEN 'PO-DEMO-202605-' || LPAD(i::text, 4, '0') WHEN i % 3 = 1 THEN 'MO-DEMO-202605-' || LPAD(i::text, 4, '0') ELSE 'SO-DEMO-202605-' || LPAD(i::text, 4, '0') END,
    901000 + i,
    DATE '2026-05-22' + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_inbound_line (
    id, inbound_id, line_no, inventory_type, sku_id, material_id, batch_no,
    planned_qty, actual_qty, location_id, unit_cost, remark, created_by, updated_by
)
SELECT
    928100 + i,
    928000 + i,
    1,
    CASE WHEN i % 3 = 1 THEN 'SKU' ELSE 'MATERIAL' END,
    CASE WHEN i % 3 = 1 THEN 916200 + i ELSE NULL END,
    CASE WHEN i % 3 = 1 THEN NULL ELSE 913000 + i END,
    CASE WHEN i % 3 = 1 THEN 'FG-DEMO-' || LPAD(i::text, 3, '0') ELSE 'RM-DEMO-' || LPAD(i::text, 3, '0') END,
    50 + i,
    CASE WHEN i % 3 = 0 THEN 50 + i ELSE 0 END,
    917100 + i,
    20 + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_outbound (
    id, outbound_no, outbound_type, warehouse_id, status, source_type,
    source_id, source_no, operator_id, outbound_date, carrier, tracking_no,
    remark, created_by, updated_by
)
SELECT
    929000 + i,
    'OUT-DEMO-202605-' || LPAD(i::text, 4, '0'),
    CASE WHEN i % 2 = 0 THEN 'SALES' ELSE 'MATERIAL' END,
    917000 + i,
    CASE WHEN i % 4 = 0 THEN 'DRAFT' WHEN i % 4 = 1 THEN 'CONFIRMED' WHEN i % 4 = 2 THEN 'PICKING' ELSE 'SHIPPED' END,
    CASE WHEN i % 2 = 0 THEN 'SALES_ORDER' ELSE 'PRODUCTION_ORDER' END,
    CASE WHEN i % 2 = 0 THEN 920000 + i ELSE 921000 + i END,
    CASE WHEN i % 2 = 0 THEN 'SO-DEMO-202605-' || LPAD(i::text, 4, '0') ELSE 'MO-DEMO-202605-' || LPAD(i::text, 4, '0') END,
    901000 + i,
    DATE '2026-05-25' + i,
    '演示承运商',
    'TRK-DEMO-' || LPAD(i::text, 5, '0'),
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_outbound_line (
    id, outbound_id, line_no, inventory_type, sku_id, material_id, batch_no,
    planned_qty, actual_qty, location_id, allocation_id, remark, created_by, updated_by
)
SELECT
    929100 + i,
    929000 + i,
    1,
    CASE WHEN i % 2 = 0 THEN 'SKU' ELSE 'MATERIAL' END,
    CASE WHEN i % 2 = 0 THEN 916200 + i ELSE NULL END,
    CASE WHEN i % 2 = 0 THEN NULL ELSE 913000 + i END,
    CASE WHEN i % 2 = 0 THEN 'FG-DEMO-' || LPAD(i::text, 3, '0') ELSE 'RM-DEMO-' || LPAD(i::text, 3, '0') END,
    10 + i,
    CASE WHEN i % 4 = 3 THEN 10 + i ELSE 0 END,
    917100 + i,
    CASE WHEN i % 2 = 0 THEN 927400 + i ELSE NULL END,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_inventory_stocktaking (
    id, stocktaking_no, stocktaking_type, count_mode, warehouse_id, zone_code,
    status, planned_date, started_at, completed_at, reviewer_id, reviewed_at,
    remark, created_by, updated_by
)
SELECT
    930000 + i,
    'ST-DEMO-202605-' || LPAD(i::text, 4, '0'),
    CASE WHEN i % 3 = 0 THEN 'FULL' WHEN i % 3 = 1 THEN 'CYCLE' ELSE 'SAMPLE' END,
    CASE WHEN i % 2 = 0 THEN 'OPEN' ELSE 'BLIND' END,
    917000 + i,
    'A',
    CASE WHEN i % 4 = 0 THEN 'DRAFT' WHEN i % 4 = 1 THEN 'IN_PROGRESS' WHEN i % 4 = 2 THEN 'DIFF_REVIEW' ELSE 'COMPLETED' END,
    DATE '2026-05-28' + i,
    CASE WHEN i % 4 > 0 THEN NOW() - INTERVAL '2 hours' ELSE NULL END,
    CASE WHEN i % 4 = 3 THEN NOW() ELSE NULL END,
    901000 + i,
    CASE WHEN i % 4 = 3 THEN NOW() ELSE NULL END,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_inventory_stocktaking_line (
    id, stocktaking_id, inventory_type, sku_id, material_id, warehouse_id,
    location_id, batch_no, system_qty, actual_qty, diff_qty, diff_status,
    diff_reason, adjusted_qty, count_by_1, count_at_1, need_recheck,
    remark, created_by, updated_by
)
SELECT
    930100 + i,
    930000 + i,
    CASE WHEN i % 2 = 0 THEN 'SKU' ELSE 'MATERIAL' END,
    CASE WHEN i % 2 = 0 THEN 916200 + i ELSE NULL END,
    CASE WHEN i % 2 = 0 THEN NULL ELSE 913000 + i END,
    917000 + i,
    917100 + i,
    CASE WHEN i % 2 = 0 THEN 'FG-DEMO-' || LPAD(i::text, 3, '0') ELSE 'RM-DEMO-' || LPAD(i::text, 3, '0') END,
    100 + i,
    CASE WHEN i % 3 = 0 THEN 98 + i ELSE 100 + i END,
    CASE WHEN i % 3 = 0 THEN -2 ELSE 0 END,
    CASE WHEN i % 3 = 0 THEN 'CONFIRMED' ELSE 'PENDING' END,
    CASE WHEN i % 3 = 0 THEN 'NORMAL_ERROR' ELSE NULL END,
    CASE WHEN i % 3 = 0 THEN 98 + i ELSE NULL END,
    901000 + i,
    NOW(),
    i % 10 = 0,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_inventory_alert_rule (
    id, rule_code, rule_name, alert_type, condition_type, threshold_value,
    category_id, warehouse_id, enabled, created_by, updated_by
)
SELECT
    931000 + i,
    'ALERT-DEMO-' || LPAD(i::text, 3, '0'),
    '演示预警规则' || LPAD(i::text, 2, '0'),
    CASE WHEN i % 3 = 0 THEN 'LOW_STOCK' WHEN i % 3 = 1 THEN 'OVERSTOCK' ELSE 'AGING' END,
    CASE WHEN i % 2 = 0 THEN 'FIXED_VALUE' ELSE 'DAYS_OF_SUPPLY' END,
    10 + i,
    910000 + i,
    917000 + i,
    TRUE,
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_inventory_alert (
    id, rule_id, alert_type, inventory_type, sku_id, material_id, warehouse_id,
    current_value, threshold_value, status, acknowledged_by, acknowledged_at,
    resolved_at, updated_by
)
SELECT
    931100 + i,
    931000 + i,
    CASE WHEN i % 3 = 0 THEN 'LOW_STOCK' WHEN i % 3 = 1 THEN 'OVERSTOCK' ELSE 'AGING' END,
    CASE WHEN i % 2 = 0 THEN 'SKU' ELSE 'MATERIAL' END,
    CASE WHEN i % 2 = 0 THEN 916200 + i ELSE NULL END,
    CASE WHEN i % 2 = 0 THEN NULL ELSE 913000 + i END,
    917000 + i,
    5 + i,
    10 + i,
    CASE WHEN i % 3 = 0 THEN 'ACTIVE' WHEN i % 3 = 1 THEN 'ACKNOWLEDGED' ELSE 'RESOLVED' END,
    CASE WHEN i % 3 <> 0 THEN 1 ELSE NULL END,
    CASE WHEN i % 3 <> 0 THEN NOW() ELSE NULL END,
    CASE WHEN i % 3 = 2 THEN NOW() ELSE NULL END,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_receiving (
    id, receiving_no, asn_id, warehouse_id, receiving_date, status,
    receiver_id, dock_no, inbound_order_id, remark, created_by, updated_by
)
SELECT
    932000 + i,
    'RCV-DEMO-202605-' || LPAD(i::text, 4, '0'),
    926000 + i,
    917000 + i,
    DATE '2026-05-21' + i,
    CASE WHEN i % 3 = 0 THEN 'COMPLETED' ELSE 'IN_PROGRESS' END,
    901000 + i,
    'D' || LPAD(i::text, 2, '0'),
    928000 + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_receiving_line (
    id, receiving_id, asn_line_id, material_id, expected_qty, received_qty,
    roll_count, difference_qty, difference_reason, batch_no, qc_status,
    putaway_status, putaway_location_id, remark, created_by, updated_by
)
SELECT
    932100 + i,
    932000 + i,
    926100 + i,
    913000 + i,
    100 + i,
    CASE WHEN i % 3 = 0 THEN 100 + i ELSE 20 END,
    5 + (i % 5),
    CASE WHEN i % 3 = 0 THEN 0 ELSE 80 + i END,
    CASE WHEN i % 3 = 0 THEN NULL ELSE 'SHORTAGE' END,
    'RM-DEMO-' || LPAD(i::text, 3, '0'),
    CASE WHEN i % 3 = 0 THEN 'PASSED' ELSE 'PENDING' END,
    CASE WHEN i % 3 = 0 THEN 'COMPLETED' ELSE 'PENDING' END,
    917100 + i,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_wave (id, wave_no, warehouse_id, strategy, status, remark, created_by, updated_by)
SELECT
    933000 + i,
    'WV-DEMO-202605-' || LPAD(i::text, 4, '0'),
    917000 + i,
    CASE WHEN i % 3 = 0 THEN 'BY_CUSTOMER' WHEN i % 3 = 1 THEN 'BY_CARRIER' ELSE 'BY_ZONE' END,
    CASE WHEN i % 4 = 0 THEN 'DRAFT' WHEN i % 4 = 1 THEN 'PICKING' WHEN i % 4 = 2 THEN 'COMPLETED' ELSE 'CANCELLED' END,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_pick_list (id, wave_id, pick_list_no, picker_id, status, remark, created_by, updated_by)
SELECT
    933100 + i,
    933000 + i,
    'PK-DEMO-202605-' || LPAD(i::text, 4, '0'),
    901000 + i,
    CASE WHEN i % 3 = 0 THEN 'COMPLETED' WHEN i % 3 = 1 THEN 'PICKING' ELSE 'DISCREPANCY' END,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;

INSERT INTO t_warehouse_pick_item (
    id, pick_list_id, outbound_line_id, sku_id, location_id, batch_no,
    planned_qty, actual_qty, status, remark, created_by, updated_by
)
SELECT
    933200 + i,
    933100 + i,
    929100 + i,
    916200 + i,
    917100 + i,
    'FG-DEMO-' || LPAD(i::text, 3, '0'),
    10 + i,
    CASE WHEN i % 3 = 0 THEN 10 + i ELSE NULL END,
    CASE WHEN i % 3 = 0 THEN 'COMPLETED' WHEN i % 3 = 1 THEN 'PICKING' ELSE 'SHORT' END,
    'V54 demo seed',
    1,
    1
FROM generate_series(1, 30) AS i
ON CONFLICT (id) DO NOTHING;
