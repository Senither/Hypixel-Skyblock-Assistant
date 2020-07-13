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

package com.senither.hypixel.database.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.database.DatabaseManager;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.time.Carbon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerDonationController {

    private static final Logger log = LoggerFactory.getLogger(PlayerDonationController.class);

    public static final Cache<String, PlayerDonationEntry> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .build();

    public static synchronized PlayerDonationEntry getPlayerByUuid(DatabaseManager manager, long guildId, UUID uuid) {
        return getPlayerByUuid(manager, guildId, uuid, true);
    }

    public static synchronized PlayerDonationEntry getPlayerByUuid(DatabaseManager manager, long guildId, UUID uuid, boolean createIfNotExists) {
        final String cacheKey = guildId + "-" + uuid.toString();

        PlayerDonationEntry cacheEntry = cache.getIfPresent(cacheKey);
        if (cacheEntry != null) {
            log.debug("Found player donation entry for {} on {} using the in-memory cache", uuid, guildId);
            return cacheEntry;
        }

        try {
            Collection result = manager.query("SELECT * FROM `donation_points` WHERE `discord_id` = ? AND `uuid` = ?", guildId, uuid);
            if (result.isEmpty()) {
                if (!createIfNotExists) {
                    return null;
                }

                log.debug("Failed to find player donation entry for {} on {}, creating new entry", uuid, guildId);

                manager.queryInsert("INSERT INTO `donation_points` (`uuid`, `discord_id`) VALUES (?, ?)",
                    uuid, guildId
                );

                PlayerDonationEntry defaultEntry = new PlayerDonationEntry(guildId, uuid);
                cache.put(cacheKey, defaultEntry);

                return defaultEntry;
            }

            log.debug("Found player donation entry for {} on {} using the database cache", uuid, guildId);

            PlayerDonationEntry playerEntry = new PlayerDonationEntry(result.first());
            cache.put(cacheKey, playerEntry);

            return playerEntry;
        } catch (SQLException e) {
            log.debug("Failed to find any player donation entry for {} on {}!", uuid, guildId);

            return null;
        }
    }

    public static List<PlayerDonationEntry> getPlayersById(DatabaseManager manager, long discordId) {
        try {
            List<PlayerDonationEntry> players = new ArrayList<>();
            for (DataRow row : manager.query("SELECT * FROM `donation_points` WHERE `discord_id` = ? ORDER BY `points` DESC", discordId)) {
                PlayerDonationEntry donationEntry = new PlayerDonationEntry(row);
                cache.put(discordId + "-" + donationEntry.getUuid(), donationEntry);

                players.add(donationEntry);
            }
            return players;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static class PlayerDonationEntry {

        private final UUID uuid;
        private final long discordId;
        private final Carbon lastCheckedAt;
        private final Carbon lastDonatedAt;
        private long points;

        PlayerDonationEntry(long discordId, UUID uuid) {
            this.discordId = discordId;
            this.uuid = uuid;

            points = 0;
            lastCheckedAt = Carbon.now();
            lastDonatedAt = Carbon.now();
        }

        PlayerDonationEntry(DataRow row) {
            uuid = UUID.fromString(row.getString("uuid"));
            discordId = row.getLong("discord_id");
            points = row.getLong("points");
            lastCheckedAt = row.getTimestamp("last_checked_at");
            lastDonatedAt = row.getTimestamp("last_donated_at");
        }

        public UUID getUuid() {
            return uuid;
        }

        public long getDiscordId() {
            return discordId;
        }

        public long getPoints() {
            return points;
        }

        public void setPoints(long points) {
            this.points = points;
        }

        public Carbon getLastCheckedAt() {
            return lastCheckedAt;
        }

        public Carbon getLastDonatedAt() {
            return lastDonatedAt;
        }
    }
}
