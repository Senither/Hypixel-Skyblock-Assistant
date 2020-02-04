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
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.inventory.Item;
import com.senither.hypixel.inventory.ItemType;
import com.senither.hypixel.rank.RankCheckResponse;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TalismansChecker extends RankRequirementChecker {

    @Override
    public RankCheckResponse getRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerUUID.toString().replace("-", ""));

        if (!isInventoryApiEnabled(member)) {
            throw new FriendlyException("Inventory API is disabled, unable to look for talismans");
        }

        try {
            List<Item> talismans = new ArrayList<>();
            talismans.addAll(buildInventoryForPlayer(member, "ender_chest_contents").getItemsWithType(ItemType.ACCESSORY));
            talismans.addAll(buildInventoryForPlayer(member, "talisman_bag").getItemsWithType(ItemType.ACCESSORY));
            talismans.addAll(buildInventoryForPlayer(member, "inv_contents").getItemsWithType(ItemType.ACCESSORY));

            int epics = 0;
            int legendaries = 0;

            for (Item talisman : talismans) {
                switch (talisman.getRarity()) {
                    case LEGENDARY:
                        legendaries++;
                        break;
                    case EPIC:
                        epics++;
                        break;
                }
            }

            for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
                if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                    continue;
                }

                GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
                if (requirement.getTalismansLegendary() <= legendaries && requirement.getTalismansEpic() <= epics) {
                    return createResponse(rank, legendaries, epics);
                }
            }

            return createResponse(null, legendaries, epics);
        } catch (IOException ignored) {
            return null;
        }
    }

    private RankCheckResponse createResponse(GuildReply.Guild.Rank rank, int legendaries, int epics) {
        return new RankCheckResponse(rank, new HashMap<String, Object>() {{
            put("legendaries", legendaries);
            put("epics", epics);
        }});
    }

    private boolean isInventoryApiEnabled(JsonObject json) {
        return json.has("ender_chest_contents")
            && json.has("talisman_bag")
            && json.has("inv_contents");
    }
}
