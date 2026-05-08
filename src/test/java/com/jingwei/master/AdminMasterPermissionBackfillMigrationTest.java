package com.jingwei.master;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminMasterPermissionBackfillMigrationTest {

    private static final Path MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V48__restore_admin_customer_supplier_permissions.sql"
    );

    @Test
    @DisplayName("ADMIN客户和供应商权限种子数据应恢复为可用")
    void shouldRestoreAdminCustomerAndSupplierPermissions() throws IOException {
        assertTrue(Files.exists(MIGRATION_PATH), "缺少客户和供应商权限恢复迁移");

        String migrationSql = Files.readString(MIGRATION_PATH);

        assertTrue(migrationSql.contains("name = '基础数据'"));
        assertTrue(migrationSql.contains("name = '客户管理'"));
        assertTrue(migrationSql.contains("name = '供应商管理'"));
        assertTrue(migrationSql.contains("master:customer:create"));
        assertTrue(migrationSql.contains("master:customer:update"));
        assertTrue(migrationSql.contains("master:customer:activate"));
        assertTrue(migrationSql.contains("master:customer:deactivate"));
        assertTrue(migrationSql.contains("master:customer:delete"));
        assertTrue(migrationSql.contains("master:supplier:create"));
        assertTrue(migrationSql.contains("master:supplier:update"));
        assertTrue(migrationSql.contains("master:supplier:activate"));
        assertTrue(migrationSql.contains("master:supplier:deactivate"));
        assertTrue(migrationSql.contains("master:supplier:delete"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (200, 230, 231, 232, 233, 234, 235, 240, 241, 242, 243, 244, 245)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
