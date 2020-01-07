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

import com.senither.hypixel.config.Configuration;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.Executors;

public class MySQLConnection extends DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(MySQLConnection.class);

    public MySQLConnection(SkyblockAssistant app) {
        super(app);
    }

    public boolean open() throws SQLException {
        try {
            Configuration.Database databaseConfiguration = app.getConfiguration().getDatabase();
            String url = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true",
                databaseConfiguration.getHostname(), databaseConfiguration.getPort(), databaseConfiguration.getDatabase()
            );

            if (initialize()) {
                connection = DriverManager.getConnection(url, databaseConfiguration.getUsername(), databaseConfiguration.getPassword());

                // Sets a timeout of 20 seconds(This is an extremely long time, however the default
                // is around 10 minutes so this should give some improvements with the threads
                // not being blocked for ages due to hanging database queries.
                connection.setNetworkTimeout(Executors.newCachedThreadPool(), 1000 * 20);

                return true;
            }
        } catch (SQLException ex) {
            String reason = "Could not establish a MySQL connection, SQLException: " + ex.getMessage();

            log.error(reason, ex);
            throw new SQLException(reason);
        }

        return false;
    }

    @Override
    protected boolean initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.MysqlDataSource");

            return true;
        } catch (ClassNotFoundException ex) {
            log.error("MySQL DataSource class missing.", ex);
        }

        return false;
    }

    @Override
    public boolean hasTable(String table) {
        try {
            DatabaseMetaData md = getConnection().getMetaData();

            try (ResultSet tables = md.getTables(null, null, table, new String[]{"TABLE"})) {
                if (tables.next()) {
                    tables.close();

                    return true;
                }
            }
        } catch (SQLException ex) {
            log.error(String.format("Failed to check if table exists \"%s\": %s", table, ex.getMessage()), ex);
        }

        return false;
    }

    @Override
    public boolean truncate(String table) {
        try {
            if (!hasTable(table)) {
                return false;
            }

            try (Statement statement = getConnection().createStatement()) {
                statement.executeUpdate(String.format("DELETE FROM `%s`;", table));
            }

            return true;
        } catch (SQLException ex) {
            log.error(String.format("Failed to truncate \"%s\": %s", table, ex.getMessage()), ex);
        }

        return false;
    }

    @Override
    public Connection getRawConnection() {
        return connection;
    }
}
