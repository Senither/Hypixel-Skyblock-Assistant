package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class AddDonationNotificationChannelColumnToGuildsTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds` ADD `donation_notification_channel` BIGINT NULL DEFAULT NULL AFTER `donation_channel`;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds` DROP `donation_notification_channel`;"
        );
    }
}
