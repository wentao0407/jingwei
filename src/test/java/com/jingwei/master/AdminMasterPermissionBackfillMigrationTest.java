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
    private static final Path CATEGORY_MATERIAL_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V49__restore_admin_category_material_permissions.sql"
    );
    private static final Path MASTER_CODE_RULE_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V50__restore_master_code_rules.sql"
    );
    private static final Path SPU_SIZE_GROUP_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V51__restore_admin_spu_size_group_permissions.sql"
    );
    private static final Path SEASON_WAREHOUSE_MIGRATION_PATH = Path.of(
            "src/main/resources/db/migration/V52__restore_admin_season_warehouse_permissions.sql"
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

    @Test
    @DisplayName("ADMIN物料分类和物料管理权限种子数据应恢复为可用")
    void shouldRestoreAdminCategoryAndMaterialPermissions() throws IOException {
        assertTrue(Files.exists(CATEGORY_MATERIAL_MIGRATION_PATH), "缺少物料分类和物料管理权限恢复迁移");

        String migrationSql = Files.readString(CATEGORY_MATERIAL_MIGRATION_PATH);

        assertTrue(migrationSql.contains("name = '基础数据'"));
        assertTrue(migrationSql.contains("name = '物料管理'"));
        assertTrue(migrationSql.contains("name = '物料分类'"));
        assertTrue(migrationSql.contains("master:material:create"));
        assertTrue(migrationSql.contains("master:material:update"));
        assertTrue(migrationSql.contains("master:material:deactivate"));
        assertTrue(migrationSql.contains("master:material:attributeDefs"));
        assertTrue(migrationSql.contains("master:category:create"));
        assertTrue(migrationSql.contains("master:category:update"));
        assertTrue(migrationSql.contains("master:category:delete"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (200, 210, 211, 212, 213, 214, 270, 271, 272, 273)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }

    @Test
    @DisplayName("主数据编码规则和规则段应恢复为可用")
    void shouldRestoreMasterCodeRules() throws IOException {
        assertTrue(Files.exists(MASTER_CODE_RULE_MIGRATION_PATH), "缺少主数据编码规则恢复迁移");

        String migrationSql = Files.readString(MASTER_CODE_RULE_MIGRATION_PATH);

        assertTrue(migrationSql.contains("MATERIAL_CODE"));
        assertTrue(migrationSql.contains("SUPPLIER_CODE"));
        assertTrue(migrationSql.contains("CUSTOMER_CODE"));
        assertTrue(migrationSql.contains("t_md_coding_rule_segment"));
        assertTrue(migrationSql.contains("segment_type = 'SEQUENCE'"));
        assertTrue(migrationSql.contains("seq_length = 6"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }

    @Test
    @DisplayName("ADMIN款式SKU和尺码组权限种子数据应恢复为可用")
    void shouldRestoreAdminSpuAndSizeGroupPermissions() throws IOException {
        assertTrue(Files.exists(SPU_SIZE_GROUP_MIGRATION_PATH), "缺少款式SKU和尺码组权限恢复迁移");

        String migrationSql = Files.readString(SPU_SIZE_GROUP_MIGRATION_PATH);

        assertTrue(migrationSql.contains("name = '基础数据'"));
        assertTrue(migrationSql.contains("name = '款式管理'"));
        assertTrue(migrationSql.contains("name = '尺码组管理'"));
        assertTrue(migrationSql.contains("master:spu:create"));
        assertTrue(migrationSql.contains("master:spu:update"));
        assertTrue(migrationSql.contains("master:spu:deactivate"));
        assertTrue(migrationSql.contains("master:spu:addColor"));
        assertTrue(migrationSql.contains("master:sku:updatePrice"));
        assertTrue(migrationSql.contains("master:sku:deactivate"));
        assertTrue(migrationSql.contains("master:sizeGroup:create"));
        assertTrue(migrationSql.contains("master:sizeGroup:update"));
        assertTrue(migrationSql.contains("master:sizeGroup:delete"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (200, 220, 221, 222, 223, 224, 225, 226, 280, 281, 282, 283, 284, 285, 286)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }

    @Test
    @DisplayName("ADMIN季节波段和仓库库位权限种子数据应恢复为可用")
    void shouldRestoreAdminSeasonAndWarehousePermissions() throws IOException {
        assertTrue(Files.exists(SEASON_WAREHOUSE_MIGRATION_PATH), "缺少季节波段和仓库库位权限恢复迁移");

        String migrationSql = Files.readString(SEASON_WAREHOUSE_MIGRATION_PATH);

        assertTrue(migrationSql.contains("name = '基础数据'"));
        assertTrue(migrationSql.contains("name = '季节波段'"));
        assertTrue(migrationSql.contains("name = '仓库库位'"));
        assertTrue(migrationSql.contains("master:season:create"));
        assertTrue(migrationSql.contains("master:season:update"));
        assertTrue(migrationSql.contains("master:season:close"));
        assertTrue(migrationSql.contains("master:season:delete"));
        assertTrue(migrationSql.contains("master:wave:create"));
        assertTrue(migrationSql.contains("master:wave:update"));
        assertTrue(migrationSql.contains("master:wave:delete"));
        assertTrue(migrationSql.contains("master:warehouse:create"));
        assertTrue(migrationSql.contains("master:warehouse:update"));
        assertTrue(migrationSql.contains("master:warehouse:activate"));
        assertTrue(migrationSql.contains("master:warehouse:deactivate"));
        assertTrue(migrationSql.contains("master:warehouse:delete"));
        assertTrue(migrationSql.contains("master:location:create"));
        assertTrue(migrationSql.contains("master:location:update"));
        assertTrue(migrationSql.contains("master:location:freeze"));
        assertTrue(migrationSql.contains("master:location:unfreeze"));
        assertTrue(migrationSql.contains("master:location:deactivate"));
        assertTrue(migrationSql.contains("master:location:delete"));
        assertTrue(migrationSql.contains("role_code = 'ADMIN'"));
        assertTrue(migrationSql.contains("menu_id IN (200, 250, 251, 252, 253, 254, 255, 256, 257, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311)"));
        assertTrue(migrationSql.contains("deleted = FALSE"));
    }
}
