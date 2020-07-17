package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class CreateBlacklistTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "CREATE TABLE `blacklists`(\n" +
                "    `id` BIGINT UNSIGNED NOT NULL,\n" +
                "    `expires_in` DATETIME NULL,\n" +
                "    `reason` VARCHAR(256) NOT NULL,\n" +
                "    INDEX(`id`)\n" +
                ") ENGINE = INNODB;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate("DROP TABLE `blacklists`");
    }
}
