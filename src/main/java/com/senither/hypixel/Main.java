/*
 * Copyright (c) 2019.
 *
 * This file is part of Hypixel Guild Synchronizer.
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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, LoginException {
        Configuration configuration = loadConfiguration();
        if (configuration == null) {
            log.info("Configuration returned null, failed to load the config.");
            shutdown(0);
        }
        new GuildSynchronize(configuration);
    }

    private static void shutdown(int code) {
        log.info("Shutting down process with code {}", code);
        System.exit(code);
    }

    private static Configuration loadConfiguration() throws IOException {
        File file = new File("config.json");
        if (!file.exists()) {
            log.info("The config.json file was not found!");
            shutdown(0);
        }

        if (!(file.canRead() || file.canWrite())) {
            log.info("The config file cannot be read or written to!");
            System.exit(0);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return new Gson().fromJson(sb.toString(), Configuration.class);
        }
    }
}
