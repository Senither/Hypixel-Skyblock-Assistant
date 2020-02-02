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

package com.senither.hypixel.rank.checkers;

import com.google.gson.JsonObject;
import com.senither.hypixel.contracts.rank.RankRequirementChecker;
import com.senither.hypixel.database.controller.GuildController;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.UUID;

public class SlayerChecker extends RankRequirementChecker {

    @Override
    public GuildReply.Guild.Rank getRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerUUID.toString().replace("-", ""));

        long totalExperience = 0;
        JsonObject slayerBosses = member.getAsJsonObject("slayer_bosses");
        for (String type : slayerBosses.keySet()) {
            try {
                totalExperience += slayerBosses.get(type).getAsJsonObject().get("xp").getAsLong();
            } catch (Exception ignored) {
            }
        }

        for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
            if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                continue;
            }

            GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
            if (requirement.getSlayerExperience() <= totalExperience) {
                return rank;
            }
        }
        return null;
    }
}
