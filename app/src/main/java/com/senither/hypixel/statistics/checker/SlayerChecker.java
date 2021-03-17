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

package com.senither.hypixel.statistics.checker;

import com.google.gson.JsonObject;
import com.senither.hypixel.contracts.statistics.Checker;
import com.senither.hypixel.contracts.statistics.SlayerFunction;
import com.senither.hypixel.statistics.responses.SlayerResponse;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class SlayerChecker extends Checker<SlayerResponse> {

    private static final Logger log = LoggerFactory.getLogger(SlayerChecker.class);

    @Override
    public SlayerResponse checkUser(@Nullable PlayerReply playerReply, SkyBlockProfileReply profileReply, JsonObject member) {
        JsonObject slayerBosses = member.getAsJsonObject("slayer_bosses");
        long totalCoinsSpentOnSlayers = getTotalCoinsSpentOnSlayers(slayerBosses);
        if (totalCoinsSpentOnSlayers == 0) {
            return new SlayerResponse(false);
        }

        SlayerResponse response = new SlayerResponse(true)
            .setTotalCoinsSpent(totalCoinsSpentOnSlayers)
            .setTotalSlayerExperience(getTotalCombinedSlayerExperience(slayerBosses));

        generateSlayerStatsResponse(slayerBosses.getAsJsonObject("zombie"), response::setRevenant);
        generateSlayerStatsResponse(slayerBosses.getAsJsonObject("spider"), response::setTarantula);
        generateSlayerStatsResponse(slayerBosses.getAsJsonObject("wolf"), response::setSven);

        return response;
    }

    private void generateSlayerStatsResponse(JsonObject slayer, SlayerFunction function) {
        function.setValue(
            getEntryFromSlayerData(slayer, "xp"),
            getEntryFromSlayerData(slayer, "boss_kills_tier_0"),
            getEntryFromSlayerData(slayer, "boss_kills_tier_1"),
            getEntryFromSlayerData(slayer, "boss_kills_tier_2"),
            getEntryFromSlayerData(slayer, "boss_kills_tier_3"),
            getEntryFromSlayerData(slayer, "boss_kills_tier_4")
        );
    }

    private long getTotalCoinsSpentOnSlayers(JsonObject jsonObject) {
        if (jsonObject == null) {
            return 0L;
        }

        try {
            long totalCoins = 0;

            for (String type : jsonObject.keySet()) {
                JsonObject slayerType = jsonObject.getAsJsonObject(type);
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_0") * 100L;
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_1") * 2000L;
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_2") * 10000L;
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_3") * 50000L;
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_4") * 100000L;
            }

            return totalCoins;
        } catch (Exception e) {
            log.error("Exception were thrown while getting total cost, error: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int getTotalCombinedSlayerExperience(JsonObject jsonObject) {
        try {
            int totalExp = 0;

            for (String type : jsonObject.keySet()) {
                totalExp += getEntryFromSlayerData(jsonObject.getAsJsonObject(type).getAsJsonObject(), "xp");
            }

            return totalExp;
        } catch (Exception e) {
            log.error("Exception were thrown while getting total experience, error: {}", e.getMessage(), e);
            return 0;
        }
    }

    private int getEntryFromSlayerData(JsonObject jsonObject, String entry) {
        try {
            return jsonObject.get(entry).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }
}
