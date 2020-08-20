package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class AddSplashManagementRoleColumnToGuildsTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds`\n" +
                "    ADD `splash_management_role` BIGINT UNSIGNED NULL AFTER `splash_role`;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "ALTER TABLE `guilds` DROP `splash_management_role`;"
        );
    }
}
