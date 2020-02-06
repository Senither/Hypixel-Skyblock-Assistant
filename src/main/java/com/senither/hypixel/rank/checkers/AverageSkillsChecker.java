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
import com.senither.hypixel.rank.RankCheckResponse;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.*;

public class AverageSkillsChecker extends RankRequirementChecker {

    private final List<Integer> generalSkillLevels = new ArrayList<>();

    public AverageSkillsChecker() {
        generalSkillLevels.addAll(Arrays.asList(
            50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500,
            5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000
        ));

        int index = 1;
        while (generalSkillLevels.size() < 50) {
            generalSkillLevels.add(100000 * index++);
        }
    }

    @Override
    public RankCheckResponse getRankForUser(GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply, UUID playerUUID) {
        JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerUUID.toString().replace("-", ""));

        double mining = getSkillExperience(member, "experience_skill_mining");
        double foraging = getSkillExperience(member, "experience_skill_foraging");
        double enchanting = getSkillExperience(member, "experience_skill_enchanting");
        double farming = getSkillExperience(member, "experience_skill_farming");
        double combat = getSkillExperience(member, "experience_skill_combat");
        double fishing = getSkillExperience(member, "experience_skill_fishing");
        double alchemy = getSkillExperience(member, "experience_skill_alchemy");

        if (mining + foraging + enchanting + farming + combat + fishing + alchemy == 0) {
            throw new FriendlyException("Skills API is disabled, unable to calculate average skill level");
        }

        double averageSkillLevel = (
            getSkillLevelFromExperience(mining) +
                getSkillLevelFromExperience(foraging) +
                getSkillLevelFromExperience(enchanting) +
                getSkillLevelFromExperience(farming) +
                getSkillLevelFromExperience(combat) +
                getSkillLevelFromExperience(fishing) +
                getSkillLevelFromExperience(alchemy)
        ) / 7D;

        for (GuildReply.Guild.Rank rank : getSortedRanksFromGuild(guildReply)) {
            if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                continue;
            }

            GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
            if (requirement.getAverageSkills() <= Math.round(averageSkillLevel)) {
                return createResponse(rank, averageSkillLevel);
            }
        }
        return createResponse(null, averageSkillLevel);
    }

    private RankCheckResponse createResponse(GuildReply.Guild.Rank rank, double amount) {
        return new RankCheckResponse(rank, new HashMap<String, Object>() {{
            put("amount", amount);
        }});
    }

    private double getSkillLevelFromExperience(double experience) {
        int level = 0;
        for (int toRemove : generalSkillLevels) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return level;
    }

    private double getSkillExperience(JsonObject object, String name) {
        try {
            return object.get(name).getAsDouble();
        } catch (Exception e) {
            return 0D;
        }
    }
}
