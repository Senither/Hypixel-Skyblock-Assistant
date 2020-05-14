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

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.migrations.*;
import com.senither.hypixel.metrics.MetricType;
import com.senither.hypixel.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private MySQLConnection connection;
    private MigrationManager migrationManager;

    public DatabaseManager(SkyblockAssistant app) {
        connection = new MySQLConnection(app);
        migrationManager = new MigrationManager(this);

        try {
            connection.open();
            log.info("Connected to database successfully");

            log.info("Registering database migrations");
            migrationManager.register(new CreateUUIDsTableMigration());
            migrationManager.register(new CreatePlayersTableMigration());
            migrationManager.register(new CreateProfilesTableMigration());
            migrationManager.register(new CreateGuildsTableMigration());
            migrationManager.register(new AddDefaultRoleColumnToGuildsTableMigration());
            migrationManager.register(new AddAutoRenameColumnToGuildsTableMigration());
            migrationManager.register(new AddLastUpdatedAtColumnToProfileAndPlayerTableMigration());
            migrationManager.register(new AddRankRequirementsColumnToGuildsTableMigration());
            migrationManager.register(new AddLastCheckedColumnToUuidsTableMigration());
            migrationManager.register(new CreateReportsTableMigration());
            migrationManager.register(new AddGuildMemberRoleColumnToGuildsTableMigration());
            migrationManager.register(new CreateDonationPointsTableMigration());
            migrationManager.register(new AddDonationsColumnsToGuildsTableMigration());
            migrationManager.register(new AddLastDonatedAtColumnToDonationsTableMigration());
            migrationManager.register(new AddDonationsChannelColumnToGuildsTableMigration());
            migrationManager.register(new AddDonationNotificationChannelColumnToGuildsTableMigration());

            log.info("Running database migrations");
            migrationManager.migrate();
        } catch (SQLException e) {
            log.error("Failed to open database connection: {}", e.getMessage(), e);
        }
    }

    public Collection query(String sql, Object... binds) throws SQLException {
        Metrics.increment(MetricType.DB_QUERIES_RAN);
        log.debug("Running select query: {}", sql, binds);

        try (PreparedStatement statement = preparedStatement(sql, binds)) {
            if (!statement.execute()) {
                return null;
            }

            return new Collection(statement.getResultSet());
        }
    }

    public Set<Integer> queryInsert(String sql, Object... binds) throws SQLException {
        Metrics.increment(MetricType.DB_QUERIES_RAN);
        log.debug("Running insert query: {}", sql, binds);

        try (PreparedStatement statement = preparedStatement(sql, binds)) {
            statement.executeUpdate();

            Set<Integer> ids = new HashSet<>();
            ResultSet keys = statement.getGeneratedKeys();
            while (keys.next()) {
                ids.add(keys.getInt(1));
            }

            return ids;
        }
    }

    public boolean queryUpdate(String sql, Object... binds) throws SQLException {
        Metrics.increment(MetricType.DB_QUERIES_RAN);
        log.debug("Running update query: {}", sql, binds);

        try (PreparedStatement statement = preparedStatement(sql, binds)) {
            return statement.executeUpdate() > 0;
        }
    }

    public MySQLConnection getConnection() throws SQLException {
        if (!connection.isOpen()) {
            connection.open();
        }

        return connection;
    }

    private PreparedStatement preparedStatement(String sql, Object... binds) throws SQLException {
        PreparedStatement statement = getConnection().getRawConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        int index = 1;
        for (Object bind : binds) {
            statement.setString(index++, bind.toString());
        }

        return statement;
    }
}
