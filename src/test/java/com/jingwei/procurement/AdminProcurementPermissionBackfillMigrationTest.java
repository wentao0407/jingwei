package com.jingwei.procurement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminProcurementPermissionBackfillMigrationTest {

    private static final Path PROCUREMENT_PERMISSION_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V56__restore_admin_procurement_stage6_permissions.sql"
    );

    @Test
    @DisplayName("ADMIN采购与仓储 Stage 6 入口权限种子数据应恢复为可用")
    void shouldRestoreAdminProcurementStage6Permissions() throws IOException {
        assertTrue(Files.exists(PROCUREMENT_PERMISSION_MIGRATION_PATH), "缺少采购与仓储 Stage 6 权限恢复迁移");

        String migrationSql = Files.readString(PROCUREMENT_PERMISSION_MIGRATION_PATH);

        assertTrue(migrationSql.contains("'采购管理'"));
        assertTrue(migrationSql.contains("'采购订单'"));
        assertTrue(migrationSql.contains("'到货通知'"));
        assertTrue(migrationSql.contains("'BOM与MRP'"));
        assertTrue(migrationSql.contains("'收货管理'"));
        assertTrue(migrationSql.contains("'上架管理'"));
        assertTrue(migrationSql.contains("procurement:order:fire-event"));
        assertTrue(migrationSql.contains("procurement:asn:receive"));
        assertTrue(migrationSql.contains("procurement:asn:qc"));
        assertTrue(migrationSql.contains("procurement:bom:approve"));
        assertTrue(migrationSql.contains("procurement:mrp:calculate"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (3300, 3310, 3311, 3320, 3321, 3322, 3330, 3331, 3332, 3340, 3350)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
