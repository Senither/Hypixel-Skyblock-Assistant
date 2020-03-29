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
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.HashMap;
import java.util.UUID;

public class FairySoulsChecker extends RankRequirementChecker {

    public FairySoulsChecker() {
        super("Fairy Souls");
    }

    @Override
    public String getRankRequirementNote(GuildController.GuildEntry.RankRequirement requirement) {
        if (!hasRequirementsSetup(requirement)) {
            return "No Fairy Soul requirement";
        }
        return String.format("%s Minimum Fairy Souls", requirement.getFairySouls());
    }

    @Override
    public boolean hasRequirementsSetup(GuildController.GuildEntry.RankRequirement requirement) {
        return requirement.getFairySouls() != Integer.MAX_VALUE;
    }

    @Override
    public RankCheckResponse handleGetRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = getProfileMemberFromUUID(profileReply, playerUUID);

        int collectedFairySouls = member.has("fairy_souls_collected")
            ? member.get("fairy_souls_collected").getAsInt()
            : 0;

        for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
            if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                continue;
            }

            GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
            if (requirement.getFairySouls() <= collectedFairySouls) {
                return createResponse(rank, collectedFairySouls);
            }
        }
        return createResponse(null, collectedFairySouls);
    }

    private RankCheckResponse createResponse(GuildReply.Guild.Rank rank, int amount) {
        return new RankCheckResponse(rank, new HashMap<String, Object>() {{
            put("amount", amount);
        }});
    }
}
