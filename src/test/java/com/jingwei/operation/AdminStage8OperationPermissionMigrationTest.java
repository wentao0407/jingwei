package com.jingwei.operation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminStage8OperationPermissionMigrationTest {

    private static final Path STAGE8_PERMISSION_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V59__restore_admin_stage8_operation_permissions.sql"
    );

    @Test
    @DisplayName("ADMIN经营辅助通知报表成本入口权限种子数据应恢复为可用")
    void shouldRestoreAdminStage8OperationPermissions() throws IOException {
        assertTrue(Files.exists(STAGE8_PERMISSION_MIGRATION_PATH), "缺少经营辅助通知报表成本权限恢复迁移");

        String migrationSql = Files.readString(STAGE8_PERMISSION_MIGRATION_PATH);

        assertTrue(migrationSql.contains("'通知中心'"));
        assertTrue(migrationSql.contains("'我的通知'"));
        assertTrue(migrationSql.contains("'报表中心'"));
        assertTrue(migrationSql.contains("'库存台账'"));
        assertTrue(migrationSql.contains("'成本核算'"));
        assertTrue(migrationSql.contains("'成本查询'"));
        assertTrue(migrationSql.contains("notification:read"));
        assertTrue(migrationSql.contains("report:ledger:view"));
        assertTrue(migrationSql.contains("cost:query:detail"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
