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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.senither.hypixel.database.DatabaseManager;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.rank.items.PowerOrb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class GuildController {

    private static final Logger log = LoggerFactory.getLogger(GuildController.class);
    private static final Gson gson = new Gson();

    private static final Cache<Long, GuildEntry> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(60, TimeUnit.SECONDS)
        .build();

    public static GuildEntry getGuildById(DatabaseManager manager, long guildId) {
        GuildEntry cacheEntry = cache.getIfPresent(guildId);
        if (cacheEntry != null) {
            log.debug("Found guild entry for {} using the in-memory cache", guildId);
            return cacheEntry;
        }

        try {
            Collection result = manager.query("SELECT * FROM `guilds` WHERE `discord_id` = ?", guildId);
            if (result.isEmpty()) {
                return null;
            }

            log.debug("Found guild entry for {} using the database cache", guildId);

            GuildEntry guildEntry = new GuildEntry(result.first());
            cache.put(guildId, guildEntry);

            return guildEntry;
        } catch (SQLException e) {
            log.debug("Found no guild entry for {} in any cache provider!", guildId);

            return null;
        }
    }

    public static boolean deleteGuildWithId(DatabaseManager manager, long guildId) {
        try {
            manager.queryUpdate("DELETE FROM `guilds` WHERE `discord_id` = ?", guildId);

            cache.invalidate(guildId);

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void forgetCacheFor(long guildId) {
        cache.invalidate(guildId);
    }

    public static GuildEntry.RankRequirement createEmptyRankRequirement() {
        return new GuildEntry.RankRequirement();
    }

    public static class GuildEntry {

        private final String id;
        private final long discordId;
        private final String name;
        private final String data;
        private final Long defaultRole;
        private final boolean autoRename;
        private final LinkedHashMap<String, RankRequirement> rankRequirements;

        GuildEntry(DataRow row) {
            id = row.getString("id");
            discordId = row.getLong("discord_id");
            name = row.getString("name");
            data = row.getString("data");
            autoRename = row.getBoolean("auto_rename");

            long defaultRole = row.getLong("default_role", 0L);
            this.defaultRole = defaultRole == 0L ? null : defaultRole;

            rankRequirements = new LinkedHashMap<>();
            if (row.getString("rank_requirements") == null) {
                return;
            }

            JsonObject rankRequirements = gson.fromJson(row.getString("rank_requirements"), JsonObject.class);
            for (String name : rankRequirements.keySet()) {
                this.rankRequirements.put(name, new RankRequirement(rankRequirements.get(name).getAsJsonObject()));
            }
        }

        public String getId() {
            return id;
        }

        public long getDiscordId() {
            return discordId;
        }

        public String getName() {
            return name;
        }

        public String getData() {
            return data;
        }

        public Long getDefaultRole() {
            return defaultRole;
        }

        public boolean isAutoRename() {
            return autoRename;
        }

        public LinkedHashMap<String, RankRequirement> getRankRequirements() {
            return rankRequirements;
        }

        public static class RankRequirement {

            private int fairySouls = Integer.MAX_VALUE;
            private int talismansLegendary = Integer.MAX_VALUE;
            private int talismansEpic = Integer.MAX_VALUE;
            private int averageSkills = Integer.MAX_VALUE;
            private int slayerExperience = Integer.MAX_VALUE;
            private int bankCoins = Integer.MAX_VALUE;
            private PowerOrb powerOrb = null;

            private int weaponPoints = Integer.MAX_VALUE;
            private HashMap<String, Integer> weaponItems = new LinkedHashMap<>();

            private int armorPoints = Integer.MAX_VALUE;
            private HashMap<String, Integer> armorItems = new LinkedHashMap<>();

            public RankRequirement() {
                //
            }

            RankRequirement(JsonObject object) {
                this.armorPoints = loadIntegerFromObject(object, "armorPoints");
                this.armorItems = loadItemsFromObject(object, "armorItems");

                this.weaponPoints = loadIntegerFromObject(object, "weaponPoints");
                this.weaponItems = loadItemsFromObject(object, "weaponItems");

                this.talismansLegendary = loadIntegerFromObject(object, "talismansLegendary");
                this.talismansEpic = loadIntegerFromObject(object, "talismansEpic");

                this.averageSkills = loadIntegerFromObject(object, "averageSkills");
                this.fairySouls = loadIntegerFromObject(object, "fairySouls");
                this.bankCoins = loadIntegerFromObject(object, "bankCoins");
                this.slayerExperience = loadIntegerFromObject(object, "slayerExperience");
                this.powerOrb = PowerOrb.fromName(object.has("powerOrb") ? object.get("powerOrb").getAsString() : null);
            }

            public int getFairySouls() {
                return fairySouls;
            }

            public void setFairySouls(int fairySouls) {
                this.fairySouls = fairySouls;
            }

            public int getTalismansLegendary() {
                return talismansLegendary;
            }

            public void setTalismansLegendary(int talismansLegendary) {
                this.talismansLegendary = talismansLegendary;
            }

            public int getTalismansEpic() {
                return talismansEpic;
            }

            public void setTalismansEpic(int talismansEpic) {
                this.talismansEpic = talismansEpic;
            }

            public int getAverageSkills() {
                return averageSkills;
            }

            public void setAverageSkills(int averageSkills) {
                this.averageSkills = averageSkills;
            }

            public int getSlayerExperience() {
                return slayerExperience;
            }

            public void setSlayerExperience(int slayerExperience) {
                this.slayerExperience = slayerExperience;
            }

            public int getBankCoins() {
                return bankCoins;
            }

            public void setBankCoins(int bankCoins) {
                this.bankCoins = bankCoins;
            }

            public PowerOrb getPowerOrb() {
                return powerOrb;
            }

            public void setPowerOrb(PowerOrb powerOrb) {
                this.powerOrb = powerOrb;
            }

            public int getWeaponPoints() {
                return weaponPoints;
            }

            public void setWeaponPoints(int weaponPoints) {
                this.weaponPoints = weaponPoints;
            }

            public HashMap<String, Integer> getWeaponItems() {
                return weaponItems;
            }

            public void setWeaponItems(HashMap<String, Integer> weaponItems) {
                this.weaponItems = weaponItems;
            }

            public int getArmorPoints() {
                return armorPoints;
            }

            public void setArmorPoints(int armorPoints) {
                this.armorPoints = armorPoints;
            }

            public HashMap<String, Integer> getArmorItems() {
                return armorItems;
            }

            public void setArmorItems(HashMap<String, Integer> armorItems) {
                this.armorItems = armorItems;
            }

            private int loadIntegerFromObject(JsonObject object, String property) {
                return object.has(property) ? object.get(property).getAsInt() : Integer.MAX_VALUE;
            }

            private HashMap<String, Integer> loadItemsFromObject(JsonObject object, String property) {
                if (!object.has(property)) {
                    return new HashMap<>();
                }

                JsonObject items = object.get(property).getAsJsonObject();
                HashMap<String, Integer> map = new HashMap<>();

                for (String name : items.keySet()) {
                    map.put(name, items.get(name).getAsInt());
                }

                return map;
            }
        }
    }
}
