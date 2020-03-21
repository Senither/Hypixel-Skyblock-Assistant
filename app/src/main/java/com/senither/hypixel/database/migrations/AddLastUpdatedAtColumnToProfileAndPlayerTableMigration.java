/*
 * Copyright (c) 2020.
 *
 * This file is part of Hypixel Skyblock Assistant.
 *
 * Hypixel Guild Synchronizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypixel Guild Synchronizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hypixel Guild Synchronizer.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.senither.hypixel.database.migrations;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.DatabaseManager;

import java.sql.SQLException;

public class AddLastUpdatedAtColumnToProfileAndPlayerTableMigration implements Migration {
    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        databaseManager.queryUpdate(
            "ALTER TABLE `players` ADD `last_updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;"
        );
        databaseManager.queryUpdate(
            "ALTER TABLE `profiles` ADD `last_updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;"
        );
        return true;
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        databaseManager.queryUpdate(
            "ALTER TABLE `players` DROP `last_updated_at`;"
        );
        databaseManager.queryUpdate(
            "ALTER TABLE `profiles` DROP `last_updated_at`;"
        );
        return true;
    }
}
