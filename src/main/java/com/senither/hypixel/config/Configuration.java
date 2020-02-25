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

package com.senither.hypixel.config;

public class Configuration {

    private String discord_token;
    private String hypixel_token;
    private Database database;
    private Servlet servlet;

    public String getDiscordToken() {
        return discord_token;
    }

    public String getHypixelToken() {
        return hypixel_token;
    }

    public Database getDatabase() {
        return database;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public class Database {

        private String hostname;
        private String username;
        private String password;
        private String database;

        public String getHostname() {
            return hostname.split(":")[0];
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getDatabase() {
            return database;
        }

        public int getPort() {
            String[] parts = hostname.split(":");
            if (parts.length == 1) {
                return 3306;
            }

            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return 3306;
            }
        }
    }

    public class Servlet {

        private boolean enabled;
        private int port;

        public boolean isEnabled() {
            return enabled;
        }

        public int getPort() {
            return port;
        }
    }
}
