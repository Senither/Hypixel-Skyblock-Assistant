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

package com.senither.hypixel.contracts.database;

import com.senither.hypixel.SkyblockAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

public abstract class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    protected final SkyblockAssistant app;

    protected Connection connection;

    private boolean lastState;
    private long lastChecked;

    public DatabaseConnection(SkyblockAssistant app) {
        this.app = app;

        lastState = false;
        lastChecked = 0L;
    }

    public abstract boolean open() throws SQLException;

    public boolean close() throws SQLException {
        if (connection == null) {
            log.warn("Could not close connection, it is null.");
            return false;
        }

        try {
            connection.close();
            lastState = false;
            lastChecked = 0;

            return true;
        } catch (SQLException e) {
            log.warn("Could not close connection, SQLException: " + e.getMessage(), e);
        }
        return false;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (!isOpen()) {
            open();
            lastState = true;
        }

        return connection;
    }

    public final boolean isOpen() {
        return isOpen(2);
    }

    public final synchronized boolean isOpen(int seconds) {
        if (connection != null) {
            // Returns the last state if the connection was checked less than three seconds ago.
            if (System.currentTimeMillis() - 5000 < lastChecked) {
                return lastState;
            }

            try {
                if (connection.isClosed()) {
                    return false;
                }

                lastState = connection.isValid(seconds);
                lastChecked = System.currentTimeMillis() - (lastState ? 0L : 250L);

                return lastState;
            } catch (SQLException e) {
                if (e instanceof SQLNonTransientException) {
                    log.warn("Failed to check if the database connection is open due to a non transient connection exception!", e);
                }
                // If the exception type is anything else, we just ignore it.
            }
        }

        return false;
    }

    protected abstract boolean initialize();

    public abstract boolean hasTable(String table);

    public abstract boolean truncate(String table);

    public abstract Connection getRawConnection();
}
