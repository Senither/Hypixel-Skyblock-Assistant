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

package com.senither.hypixel.scheduler.jobs;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class DatabaseCacheCleanupJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCacheCleanupJob.class);

    public DatabaseCacheCleanupJob(SkyblockAssistant app) {
        super(app, 30, 90, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            app.getDatabaseManager().queryUpdate("DELETE FROM `players` WHERE `created_at` < (NOW() - INTERVAL 1 DAY)");
        } catch (SQLException e) {
            log.error("Failed to cleanup old records for player cache in the database, error: {}", e.getMessage(), e);
        }

        try {
            app.getDatabaseManager().queryUpdate("DELETE FROM `profiles` WHERE `created_at` < (NOW() - INTERVAL 1 HOUR)");
        } catch (SQLException e) {
            log.error("Failed to cleanup old records for profiles cache in the database, error: {}", e.getMessage(), e);
        }
    }
}
