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
import com.senither.hypixel.rank.items.Armor;
import com.senither.hypixel.rank.items.ArmorSet;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.io.IOException;
import java.util.*;

public class ArmorChecker extends RankRequirementChecker {

    @Override
    public GuildReply.Guild.Rank getRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerUUID.toString().replace("-", ""));

        if (!isInventoryApiEnabled(member)) {
            throw new FriendlyException("Inventory API is disabled, unable to look for armor");
        }

        try {
            List<Item> items = new ArrayList<>();
            items.addAll(buildInventoryForPlayer(member, "ender_chest_contents").getItemsWithType(ItemType.ARMOR));
            items.addAll(buildInventoryForPlayer(member, "inv_contents").getItemsWithType(ItemType.ARMOR));
            items.addAll(buildInventoryForPlayer(member, "inv_armor").getItemsWithType(ItemType.ARMOR));

            for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
                if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                    continue;
                }

                EnumMap<Armor, Integer> armors = new EnumMap<>(Armor.class);
                GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());

                for (Map.Entry<String, Integer> armorEntry : requirement.getArmorItems().entrySet()) {
                    Armor armor = Armor.getFromName(armorEntry.getKey());
                    if (armor != null) {
                        armors.put(armor, armorEntry.getValue());
                    }
                }

                int points = 0;
                for (Map.Entry<Armor, Integer> armorIntegerEntry : armors.entrySet()) {
                    int armorPieces = 0;

                    ArmorSet armorSet = armorIntegerEntry.getKey().getArmorSet();
                    for (Item item : items) {
                        if (armorSet.isPartOfSet(item.getName())) {
                            armorPieces += armorIntegerEntry.getValue();
                        }
                    }

                    if (armorPieces > armorSet.getSetPieces()) {
                        points += armorIntegerEntry.getValue();
                    }
                }

                if (points >= requirement.getWeaponPoints()) {
                    return rank;
                }
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    private boolean isInventoryApiEnabled(JsonObject json) {
        return json.has("ender_chest_contents")
            && json.has("inv_contents")
            && json.has("inv_armor");
    }
}
