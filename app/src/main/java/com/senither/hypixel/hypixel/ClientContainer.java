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

package com.senither.hypixel.hypixel;

import com.senither.hypixel.SkyblockAssistant;
import net.hypixel.api.HypixelAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;

public class ClientContainer {

    private static final Logger log = LoggerFactory.getLogger(ClientContainer.class);

    private final HypixelAPI[] clients;
    private int index = 0;

    ClientContainer(SkyblockAssistant app) {
        if (app.getConfiguration().getHypixelTokens() == null) {
            clients = new HypixelAPI[]{new HypixelAPI(UUID.fromString(app.getConfiguration().getHypixelToken()))};
        } else {
            HashSet<UUID> tokens = new HashSet<>();
            for (String token : app.getConfiguration().getHypixelTokens()) {
                try {
                    tokens.add(UUID.fromString(token));
                } catch (Exception e) {
                    log.warn("The \"{}\" API token is not a valid UUID, the key was skipped!", token);
                }
            }

            HypixelAPI[] clients = new HypixelAPI[tokens.size()];
            for (UUID token : tokens) {
                clients[index++] = new HypixelAPI(token);
            }
            this.clients = clients;
        }
    }

    public HypixelAPI[] getClients() {
        return clients;
    }

    public HypixelAPI getNextClient() {
        if (index >= clients.length) {
            index = 0;
        }
        return clients[index++];
    }
}
