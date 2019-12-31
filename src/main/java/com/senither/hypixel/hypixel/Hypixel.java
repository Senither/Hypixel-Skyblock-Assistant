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

package com.senither.hypixel.hypixel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.database.collection.Collection;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.adapters.UUIDTypeAdapter;
import net.hypixel.api.reply.PlayerReply;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Hypixel {

    private static final Logger log = LoggerFactory.getLogger(Hypixel.class);
    private static final Cache<String, UUID> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build();

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
        .create();

    private static final Pattern minecraftUsernameRegex = Pattern.compile("^\\w+$", Pattern.CASE_INSENSITIVE);
    private static final List<Integer> skillLevels = new ArrayList<>();

    static {
        skillLevels.addAll(Arrays.asList(
            50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500,
            5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000
        ));

        for (int i = 1; i < 30; i++) {
            skillLevels.add(100000 * i);
        }
    }

    private final SkyblockAssistant app;
    private final HypixelAPI hypixelAPI;
    private final HttpClient httpClient;

    public Hypixel(SkyblockAssistant app) {
        this.app = app;

        this.httpClient = HttpClientBuilder.create().build();
        this.hypixelAPI = new HypixelAPI(UUID.fromString(app.getConfiguration().getHypixelToken()));
    }

    public boolean isValidMinecraftUsername(@Nonnull String username) {
        return username.length() > 2 && username.length() < 17 && minecraftUsernameRegex.matcher(username).find();
    }

    public double getSkillLevelFromExperience(double experience) {
        int level = 0;
        for (int toRemove : skillLevels) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return 0;
    }

    public HypixelAPI getAPI() {
        return hypixelAPI;
    }

    public CompletionStage<PlayerReply> getPlayerByName(String name) {
        CompletableFuture<PlayerReply> future = new CompletableFuture<>();

        try {
            UUID uuid = getUUIDFromName(name);
            if (uuid != null) {
                return getAPI().getPlayerByUuid(uuid);
            }

            future.completeExceptionally(new RuntimeException("Failed to find a valid UUID for the given username!"));
        } catch (SQLException e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private UUID getUUIDFromName(String name) throws SQLException {
        UUID cachedUUID = cache.getIfPresent(name.toLowerCase());
        if (cachedUUID != null) {
            log.debug("Found UUID for {} using the in-memory cache (ID: {})", name, cachedUUID);
            return cachedUUID;
        }

        Collection result = app.getDatabaseManager().query("SELECT uuid FROM `uuids` WHERE `username` = ?", name);
        if (!result.isEmpty()) {
            UUID uuid = UUID.fromString(result.get(0).getString("uuid"));
            cache.put(name.toLowerCase(), uuid);
            log.debug("Found UUID for {} using the database cache (ID: {})", name, uuid);

            return uuid;
        }

        try {
            MojangPlayerUUID mojangPlayer = httpClient.execute(new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, MojangPlayerUUID.class);
            });

            if (mojangPlayer == null || mojangPlayer.getUUID() == null) {
                return null;
            }

            log.debug("Found UUID for {} using the Mojang API (ID: {})", name, mojangPlayer.getUUID());

            try {
                app.getDatabaseManager().queryInsert("INSERT INTO `uuids` SET `uuid` = ?, `username` = ?",
                    mojangPlayer.getUUID().toString(), name
                );
            } catch (Exception ignored) {
                //
            }

            return mojangPlayer.getUUID();
        } catch (IOException e) {
            log.error("Failed to fetch UUID for {} using the Mojang API, error: {}", name, e.getMessage(), e);
        }

        return null;
    }
}
