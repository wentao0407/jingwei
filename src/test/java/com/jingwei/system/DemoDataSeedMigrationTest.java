package com.jingwei.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataSeedMigrationTest {

    private static final Path MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V54__seed_demo_data_for_current_menus.sql"
    );

    @Test
    @DisplayName("演示数据迁移应覆盖当前菜单核心列表并保持跨模块关联")
    void shouldSeedDemoDataForCurrentMenus() throws IOException {
        assertTrue(Files.exists(MIGRATION_PATH), "缺少当前菜单演示数据迁移");

        String migrationSql = Files.readString(MIGRATION_PATH);

        assertTrue(migrationSql.contains("generate_series(1, 30)"));
        assertContainsTables(migrationSql,
                "t_sys_user",
                "t_sys_role",
                "t_sys_config",
                "t_md_customer",
                "t_md_supplier",
                "t_md_category",
                "t_md_material",
                "t_md_spu",
                "t_md_sku",
                "t_md_size_group",
                "t_md_season",
                "t_md_warehouse",
                "t_md_coding_rule",
                "t_order_sales",
                "t_order_production",
                "t_order_return",
                "t_bom",
                "t_procurement_mrp_result",
                "t_procurement_order",
                "t_procurement_asn",
                "t_inventory_sku",
                "t_inventory_material",
                "t_inventory_stocktaking",
                "t_inventory_alert",
                "t_warehouse_inbound",
                "t_warehouse_outbound",
                "t_warehouse_receiving",
                "t_warehouse_wave"
        );
        assertTrue(migrationSql.contains("master data -> sales order -> production/return"));
        assertTrue(migrationSql.contains("920000 + i"));
        assertTrue(migrationSql.contains("921000 + i"));
        assertTrue(migrationSql.contains("925000 + i"));
        assertFalse(migrationSql.contains("admin123"));
        assertFalse(migrationSql.contains("$2a$"));
    }

    private static void assertContainsTables(String migrationSql, String... tableNames) {
        for (String tableName : tableNames) {
            assertTrue(
                    migrationSql.contains("INSERT INTO " + tableName),
                    "演示数据迁移缺少表：" + tableName
            );
        }
    }
}
