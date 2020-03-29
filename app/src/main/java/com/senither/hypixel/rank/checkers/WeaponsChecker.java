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
import com.senither.hypixel.exceptions.NoRankRequirementException;
import com.senither.hypixel.inventory.Item;
import com.senither.hypixel.inventory.ItemType;
import com.senither.hypixel.rank.RankCheckResponse;
import com.senither.hypixel.rank.items.Weapon;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.io.IOException;
import java.util.*;

public class WeaponsChecker extends RankRequirementChecker {

    public WeaponsChecker() {
        super("Weapon");
    }

    @Override
    public String getRankRequirementNote(GuildController.GuildEntry.RankRequirement requirement) {
        if (!hasRequirementsSetup(requirement)) {
            return "No Weapon requirement";
        }

        List<String> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : requirement.getWeaponItems().entrySet()) {
            items.add(String.format("%s = %s", entry.getKey(), entry.getValue()));
        }

        return String.format("Must have %s Weapon Points, Items:\n\n%s",
            requirement.getWeaponPoints(), String.join("\n", items)
        );
    }

    @Override
    public boolean hasRequirementsSetup(GuildController.GuildEntry.RankRequirement requirement) {
        return requirement.getWeaponPoints() != Integer.MAX_VALUE && !requirement.getWeaponItems().isEmpty();
    }

    @Override
    public RankCheckResponse handleGetRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = getProfileMemberFromUUID(profileReply, playerUUID);

        if (!isInventoryApiEnabled(member)) {
            throw new FriendlyException("Inventory API is disabled, unable to look for weapons");
        }

        boolean hasRequirements = false;
        for (GuildController.GuildEntry.RankRequirement requirement : guildEntry.getRankRequirements().values()) {
            if (requirement.getWeaponPoints() != Integer.MAX_VALUE && !requirement.getWeaponItems().isEmpty()) {
                hasRequirements = true;
            }
        }

        if (!hasRequirements) {
            throw new NoRankRequirementException("weapon");
        }

        try {
            List<Item> items = new ArrayList<>();
            items.addAll(buildInventoryForPlayer(member, "ender_chest_contents").getItemsWithType(ItemType.WEAPON));
            items.addAll(buildInventoryForPlayer(member, "inv_contents").getItemsWithType(ItemType.WEAPON));

            for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
                if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                    continue;
                }

                GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
                if (requirement.getWeaponItems().isEmpty() || requirement.getWeaponPoints() == Integer.MAX_VALUE) {
                    continue;
                }

                EnumMap<Weapon, Integer> weapons = new EnumMap<>(Weapon.class);
                for (Map.Entry<String, Integer> weaponEntry : requirement.getWeaponItems().entrySet()) {
                    Weapon weapon = Weapon.getFromName(weaponEntry.getKey());
                    if (weapon != null) {
                        weapons.put(weapon, weaponEntry.getValue());
                    }
                }

                int points = 0;
                for (Map.Entry<Weapon, Integer> weaponIntegerEntry : weapons.entrySet()) {
                    for (Item item : items) {
                        if (weaponIntegerEntry.getKey().match(item)) {
                            points += weaponIntegerEntry.getValue();
                        }
                    }
                }

                if (points >= requirement.getWeaponPoints()) {
                    return createResponse(rank, points);
                }
            }
        } catch (IOException ignored) {
        }
        return createResponse(null, 0);
    }

    private RankCheckResponse createResponse(GuildReply.Guild.Rank rank, int points) {
        return new RankCheckResponse(rank, new HashMap<String, Object>() {{
            put("points", points);
        }});
    }

    private boolean isInventoryApiEnabled(JsonObject json) {
        return json.has("ender_chest_contents")
            && json.has("inv_contents");
    }
}
