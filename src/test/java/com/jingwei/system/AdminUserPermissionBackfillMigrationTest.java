package com.jingwei.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminUserPermissionBackfillMigrationTest {

    private static final Path MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V42__backfill_admin_user_permissions_by_role_code.sql"
    );
    private static final Path SEED_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V43__backfill_admin_user_management_seed.sql"
    );
    private static final Path RESTORE_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V44__restore_admin_user_management_permissions.sql"
    );

    @Test
    @DisplayName("ADMIN用户管理按钮权限回填应按角色编码匹配角色")
    void shouldBackfillAdminUserPermissionsByRoleCode() throws IOException {
        assertTrue(Files.exists(MIGRATION_PATH), "缺少 ADMIN 用户管理按钮权限回填迁移");

        String migrationSql = Files.readString(MIGRATION_PATH);

        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("system:user:create"));
        assertTrue(migrationSql.contains("system:user:update"));
        assertTrue(migrationSql.contains("system:user:deactivate"));
        assertTrue(migrationSql.contains("system:user:assignRole"));
        assertFalse(migrationSql.contains("v.role_id"));
    }

    @Test
    @DisplayName("本地库缺少用户管理菜单或admin角色绑定时应兜底回填")
    void shouldBackfillUserManagementSeedForIncompleteLocalDatabase() throws IOException {
        assertTrue(Files.exists(SEED_MIGRATION_PATH), "缺少用户管理种子数据兜底回填迁移");

        String migrationSql = Files.readString(SEED_MIGRATION_PATH);

        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("u.username = 'admin'"));
        assertTrue(migrationSql.contains("'用户管理'"));
        assertTrue(migrationSql.contains("'system:user:create'"));
        assertTrue(migrationSql.contains("'system:user:update'"));
        assertTrue(migrationSql.contains("'system:user:deactivate'"));
        assertTrue(migrationSql.contains("'system:user:assignRole'"));
        assertTrue(migrationSql.contains("ON CONFLICT (id) DO NOTHING"));
        assertTrue(migrationSql.contains("t_sys_user_role"));
        assertTrue(migrationSql.contains("t_sys_role_menu"));
    }

    @Test
    @DisplayName("被软删除的admin用户管理权限种子数据应恢复为可用")
    void shouldRestoreSoftDeletedAdminUserManagementPermissions() throws IOException {
        assertTrue(Files.exists(RESTORE_MIGRATION_PATH), "缺少软删除权限种子数据恢复迁移");

        String migrationSql = Files.readString(RESTORE_MIGRATION_PATH);

        assertTrue(migrationSql.contains("UPDATE t_sys_role"));
        assertTrue(migrationSql.contains("UPDATE t_sys_menu"));
        assertTrue(migrationSql.contains("UPDATE t_sys_user_role"));
        assertTrue(migrationSql.contains("UPDATE t_sys_role_menu"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("username = 'admin'"));
        assertTrue(migrationSql.contains("permission = 'system:user:create'"));
        assertTrue(migrationSql.contains("permission = 'system:user:update'"));
        assertTrue(migrationSql.contains("permission = 'system:user:deactivate'"));
        assertTrue(migrationSql.contains("permission = 'system:user:assignRole'"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
