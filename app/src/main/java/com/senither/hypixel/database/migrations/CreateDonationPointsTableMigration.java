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

public class CreateDonationPointsTableMigration implements Migration {

    @Override
    public boolean up(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate(
            "CREATE TABLE `donation_points`(\n" +
                "    `uuid` VARCHAR(64) NOT NULL,\n" +
                "    `discord_id` BIGINT UNSIGNED NOT NULL,\n" +
                "    `points` BIGINT NOT NULL DEFAULT '0',\n" +
                "    `last_checked_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "     PRIMARY KEY (`uuid`, `discord_id`)\n" +
                ") ENGINE = InnoDB;"
        );
    }

    @Override
    public boolean down(DatabaseManager databaseManager) throws SQLException {
        return databaseManager.queryUpdate("DROP TABLE `donation_points`");
    }
}
