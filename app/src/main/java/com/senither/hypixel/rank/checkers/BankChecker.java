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
import com.senither.hypixel.utils.NumberUtil;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.HashMap;
import java.util.UUID;

public class BankChecker extends RankRequirementChecker {

    public BankChecker() {
        super("Bank");
    }

    @Override
    public String getRankRequirementNote(GuildController.GuildEntry.RankRequirement requirement) {
        if (!hasRequirementsSetup(requirement)) {
            return "No Bank requirement";
        }
        return String.format("Must have at least %s coins", NumberUtil.formatNicely(requirement.getBankCoins()));
    }

    @Override
    public boolean hasRequirementsSetup(GuildController.GuildEntry.RankRequirement requirement) {
        return requirement.getBankCoins() != Integer.MAX_VALUE;
    }

    @Override
    public RankCheckResponse handleGetRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = getProfileMemberFromUUID(profileReply, playerUUID);

        int bank = 0;
        int purse = member.get("coin_purse").getAsInt();

        if (profileReply.getProfile().has("banking")) {
            bank = profileReply.getProfile().get("banking").getAsJsonObject().get("balance").getAsInt();
        }

        int totalCoins = bank + purse;
        for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
            if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                continue;
            }

            GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
            if (requirement.getBankCoins() <= totalCoins) {
                return createResponse(rank, bank, purse);
            }
        }
        return createResponse(null, bank, purse);
    }

    private RankCheckResponse createResponse(GuildReply.Guild.Rank rank, int bank, int purse) {
        return new RankCheckResponse(rank, new HashMap<String, Object>() {{
            put("amount", bank + purse);
            put("bank", bank);
            put("purse", purse);
        }});
    }
}
