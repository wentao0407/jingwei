package com.jingwei.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminOrderPermissionBackfillMigrationTest {

    private static final Path ORDER_PERMISSION_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V55__restore_admin_production_return_permissions.sql"
    );

    @Test
    @DisplayName("ADMIN生产订单和退货入口权限种子数据应恢复为可用")
    void shouldRestoreAdminProductionAndReturnPermissions() throws IOException {
        assertTrue(Files.exists(ORDER_PERMISSION_MIGRATION_PATH), "缺少生产订单和退货入口权限恢复迁移");

        String migrationSql = Files.readString(ORDER_PERMISSION_MIGRATION_PATH);

        assertTrue(migrationSql.contains("'订单管理'"));
        assertTrue(migrationSql.contains("'销售订单'"));
        assertTrue(migrationSql.contains("'生产订单'"));
        assertTrue(migrationSql.contains("order:sales:convert"));
        assertTrue(migrationSql.contains("order:sales:quantity-change"));
        assertTrue(migrationSql.contains("order:return:create"));
        assertTrue(migrationSql.contains("order:production:fire-event"));
        assertTrue(migrationSql.contains("order:production:fire-line-event"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (3200, 3210, 3217, 3218, 3219, 3220, 3221, 3222)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
