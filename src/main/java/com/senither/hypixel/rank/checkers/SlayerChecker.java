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
import com.senither.hypixel.rank.RankCheckResponse;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.SlayerResponse;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.HashMap;
import java.util.UUID;

public class SlayerChecker extends RankRequirementChecker {

    public SlayerChecker() {
        super("Slayer");
    }

    @Override
    public String getRankRequirementNote(GuildController.GuildEntry.RankRequirement requirement) {
        if (!hasRequirementsSetup(requirement)) {
            return "No Slayer requirement";
        }
        return String.format("Must have %s Slayer XP", requirement.getSlayerExperience());
    }

    @Override
    public boolean hasRequirementsSetup(GuildController.GuildEntry.RankRequirement requirement) {
        return requirement.getSlayerExperience() != Integer.MAX_VALUE;
    }

    @Override
    public RankCheckResponse handleGetRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = getProfileMemberFromUUID(profileReply, playerUUID);

        SlayerResponse response = StatisticsChecker.SLAYER.checkUser(null, profileReply, member);
        for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
            if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                continue;
            }

            GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
            if (requirement.getSlayerExperience() <= response.getTotalSlayerExperience()) {
                return createResponse(rank, response.getTotalSlayerExperience());
            }
        }
        return createResponse(null, response.getTotalSlayerExperience());
    }

    private RankCheckResponse createResponse(GuildReply.Guild.Rank rank, long amount) {
        return new RankCheckResponse(rank, new HashMap<String, Object>() {{
            put("amount", amount);
        }});
    }
}
