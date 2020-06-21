package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class AddSplashPointsColumnToGuildsTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds` ADD `splash_points` BOOLEAN NOT NULL DEFAULT FALSE AFTER `splash_role`;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds` DROP `splash_points`;"
        );
    }
}
