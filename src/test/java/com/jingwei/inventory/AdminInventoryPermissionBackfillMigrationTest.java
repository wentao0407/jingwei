package com.jingwei.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminInventoryPermissionBackfillMigrationTest {

    private static final Path INVENTORY_PERMISSION_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V57__restore_admin_inventory_stage7_permissions.sql"
    );

    @Test
    @DisplayName("ADMIN库存与物流 Stage 7 首批入口权限种子数据应恢复为可用")
    void shouldRestoreAdminInventoryStage7Permissions() throws IOException {
        assertTrue(Files.exists(INVENTORY_PERMISSION_MIGRATION_PATH), "缺少库存与物流 Stage 7 权限恢复迁移");

        String migrationSql = Files.readString(INVENTORY_PERMISSION_MIGRATION_PATH);

        assertTrue(migrationSql.contains("'库存物流'"));
        assertTrue(migrationSql.contains("'库存 SKU'"));
        assertTrue(migrationSql.contains("'库存物料'"));
        assertTrue(migrationSql.contains("'入库单'"));
        assertTrue(migrationSql.contains("'出库单'"));
        assertTrue(migrationSql.contains("'盘点单'"));
        assertTrue(migrationSql.contains("inventory:inbound:create"));
        assertTrue(migrationSql.contains("inventory:inbound:confirm"));
        assertTrue(migrationSql.contains("inventory:outbound:create"));
        assertTrue(migrationSql.contains("inventory:outbound:confirm"));
        assertTrue(migrationSql.contains("inventory:stocktaking:create"));
        assertTrue(migrationSql.contains("inventory:stocktaking:submit"));
        assertTrue(migrationSql.contains("inventory:stocktaking:review"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (3400, 3410, 3420, 3430, 3431, 3432, 3440, 3441, 3442, 3450, 3451, 3452, 3453)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
