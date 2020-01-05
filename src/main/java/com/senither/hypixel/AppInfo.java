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

package com.senither.hypixel;

import java.io.IOException;
import java.util.Properties;

public class AppInfo {

    private static AppInfo instance;

    public final String version;
    public final String groupId;
    public final String artifactId;

    protected final Properties properties = new Properties();

    private AppInfo() {
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load app.properties", e);
        }

        this.version = properties.getProperty("version");
        this.groupId = properties.getProperty("groupId");
        this.artifactId = properties.getProperty("artifactId");
    }

    public static AppInfo getAppInfo() {
        if (instance == null) {
            instance = new AppInfo();
        }
        return instance;
    }
}
