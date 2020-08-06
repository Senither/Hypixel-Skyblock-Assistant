package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class CreateBoopOptTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "CREATE TABLE `boop_opt`(\n" +
                "    `discord_id` BIGINT UNSIGNED NOT NULL,\n" +
                "    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    UNIQUE(`discord_id`)\n" +
                ") ENGINE = INNODB;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate("DROP TABLE `boop_opt`");
    }
}
