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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DatabaseManager {

    private final SkyblockAssistant app;

    private MySQLConnection connection;

    public DatabaseManager(SkyblockAssistant app) {
        this.app = app;

        connection = new MySQLConnection(app);
    }

    public Collection query(String sql, Object... binds) throws SQLException {
        try (PreparedStatement statement = preparedStatement(sql, binds)) {
            if (!statement.execute()) {
                return null;
            }

            return new Collection(statement.getResultSet());
        }
    }

    public Set<Integer> queryInsert(String sql, Object... binds) throws SQLException {
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
        PreparedStatement statement = getConnection().getRawConnection().prepareStatement(sql);

        int index = 1;
        for (Object bind : binds) {
            statement.setString(index++, bind.toString());
        }

        return statement;
    }
}
