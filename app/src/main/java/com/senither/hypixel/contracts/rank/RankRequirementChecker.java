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

package com.senither.hypixel.contracts.rank;

import com.google.gson.JsonObject;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.exceptions.NoRankRequirementException;
import com.senither.hypixel.inventory.Inventory;
import com.senither.hypixel.rank.RankCheckResponse;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class RankRequirementChecker {

    private final String type;

    protected RankRequirementChecker(String type) {
        this.type = type;
    }

    public RankCheckResponse getRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        checkForRankRequirementsSetup(guildEntry, type);

        return handleGetRankForUser(guildEntry, guildReply, profileReply, playerUUID);
    }

    public abstract String getRankRequirementNote(GuildController.GuildEntry.RankRequirement requirement);

    protected abstract boolean hasRequirementsSetup(GuildController.GuildEntry.RankRequirement requirement);

    protected abstract RankCheckResponse handleGetRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID);

    private void checkForRankRequirementsSetup(GuildController.GuildEntry guildEntry, String type) {
        boolean hasAtleastOneRequirementSetup = false;
        for (GuildController.GuildEntry.RankRequirement requirement : guildEntry.getRankRequirements().values()) {
            if (hasRequirementsSetup(requirement)) {
                hasAtleastOneRequirementSetup = true;
            }
        }

        if (!hasAtleastOneRequirementSetup) {
            throw new NoRankRequirementException(type);
        }
    }

    protected JsonObject getProfileMemberFromUUID(SkyBlockProfileReply profileReply, UUID playerUUID) {
        return profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerUUID.toString().replace("-", ""));
    }

    protected final List<GuildReply.Guild.Rank> getSortedRanksFromGuild(GuildReply guild) {
        return guild.getGuild().getRanks().stream()
            .sorted((o1, o2) -> o2.getPriority() - o1.getPriority())
            .collect(Collectors.toList());
    }

    protected final Inventory buildInventoryForPlayer(JsonObject member, String inventoryName) throws IOException {
        return new Inventory(member.get(inventoryName).getAsJsonObject().get("data").getAsString());
    }
}
