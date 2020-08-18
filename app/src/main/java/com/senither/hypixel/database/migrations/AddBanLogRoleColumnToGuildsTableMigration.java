package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class AddBanLogRoleColumnToGuildsTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds`\n" +
                "    ADD `ban_log_role` BIGINT UNSIGNED NULL AFTER `donation_notification_channel`;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds` DROP `ban_log_role`;"
        );
    }
}
