/*
 * Copyright (c) 2019.
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

package com.senither.hypixel.database;

import com.senither.hypixel.contracts.database.Migration;
import com.senither.hypixel.database.collection.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.LinkedHashSet;

public class MigrationManager {

    private static final Logger log = LoggerFactory.getLogger(MigrationManager.class);

    private final DatabaseManager databaseManager;
    private LinkedHashSet<Migration> migrations;

    MigrationManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.migrations = new LinkedHashSet<>();
    }

    public void register(Migration migration) {
        migrations.add(migration);
    }

    public void migrate() throws SQLException {
        createMigrationsTable();

        Collection result = databaseManager.query("SELECT * FROM `migrations`");
        for (Migration migration : migrations) {
            if (result.whereLoose("name", migration.getClass().getSimpleName()).isEmpty()) {
                log.info("Migrating \"{}\"", migration.getClass().getSimpleName());

                try {
                    migration.up(databaseManager);

                    databaseManager.queryInsert("INSERT INTO `migrations` SET name = ?", migration.getClass().getSimpleName());
                } catch (Exception e) {
                    migration.down(databaseManager);

                    log.error("{} migration failed with an exception, error: {}",
                        migration.getClass().getSimpleName(), e.getMessage(), e
                    );
                }
            }
        }
    }

    private void createMigrationsTable() throws SQLException {
        databaseManager.queryUpdate(
            "CREATE TABLE IF NOT EXISTS `migrations`(\n" +
                "    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                "    `name` VARCHAR(128) NOT NULL,\n" +
                "    PRIMARY KEY(`id`)\n" +
                ") ENGINE = InnoDB;"
        );
    }
}
