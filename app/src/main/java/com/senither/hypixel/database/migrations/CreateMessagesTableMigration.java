package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class CreateMessagesTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "CREATE TABLE `messages`(\n" +
                "    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                "    `discord_id` BIGINT UNSIGNED NOT NULL,\n" +
                "    `channel_id` BIGINT UNSIGNED NOT NULL,\n" +
                "    `message_id` BIGINT UNSIGNED NOT NULL,\n" +
                "    `content` MEDIUMTEXT NOT NULL,\n" +
                "    `variables` MEDIUMTEXT NULL DEFAULT NULL,\n" +
                "    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    `updated_at` DATETIME ON UPDATE CURRENT_TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    PRIMARY KEY(`id`)\n" +
                ") ENGINE = INNODB;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate("DROP TABLE `messages`");
    }
}
