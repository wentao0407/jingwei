package com.jingwei.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminStage7Stage8FollowupPermissionMigrationTest {

    private static final Path FOLLOWUP_PERMISSION_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V58__restore_admin_stage7_stage8_followup_permissions.sql"
    );

    @Test
    @DisplayName("ADMIN库存物流后续入口与审批中心权限种子数据应恢复为可用")
    void shouldRestoreAdminStage7Stage8FollowupPermissions() throws IOException {
        assertTrue(Files.exists(FOLLOWUP_PERMISSION_MIGRATION_PATH), "缺少库存物流后续入口与审批中心权限恢复迁移");

        String migrationSql = Files.readString(FOLLOWUP_PERMISSION_MIGRATION_PATH);

        assertTrue(migrationSql.contains("'库存预警'"));
        assertTrue(migrationSql.contains("'确认预警'"));
        assertTrue(migrationSql.contains("'波次拣货'"));
        assertTrue(migrationSql.contains("'发运单'"));
        assertTrue(migrationSql.contains("'审批中心'"));
        assertTrue(migrationSql.contains("'我的审批'"));
        assertTrue(migrationSql.contains("inventory:alert:acknowledge"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (3460, 3461, 3470, 3480, 3600, 3610)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
