package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class CreateBanLogTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "CREATE TABLE `ban_log`(\n" +
                "    `discord_id` BIGINT UNSIGNED NOT NULL,\n" +
                "    `added_by` VARCHAR(64) NOT NULL,\n" +
                "    `uuid` VARCHAR(64) NOT NULL,\n" +
                "    `reason` TEXT NULL,\n" +
                "    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    INDEX(`discord_id`),\n" +
                "    INDEX(`uuid`)\n" +
                ") ENGINE = INNODB;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate("DROP TABLE `ban_log`");
    }
}
