package com.jingwei.operation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Stage8RuntimeTableMigrationTest {

    @Test
    @DisplayName("Stage 8 runtime table migration should be idempotent and cover alert/notification APIs")
    void migration_shouldEnsureRuntimeTables() throws Exception {
        String migrationSql = Files.readString(Path.of(
                "src/main/resources/db/migration/V60__ensure_stage8_runtime_tables.sql"
        ));

        assertTrue(migrationSql.contains("CREATE TABLE IF NOT EXISTS t_inventory_alert"));
        assertTrue(migrationSql.contains("CREATE TABLE IF NOT EXISTS t_sys_notification"));
        assertTrue(migrationSql.contains("CREATE TABLE IF NOT EXISTS t_sys_notification_receiver"));
        assertTrue(migrationSql.contains("CREATE TABLE IF NOT EXISTS t_sys_notification_preference"));
        assertTrue(migrationSql.contains("ADD COLUMN IF NOT EXISTS"));
        assertTrue(migrationSql.contains("CREATE INDEX IF NOT EXISTS"));
    }
}
