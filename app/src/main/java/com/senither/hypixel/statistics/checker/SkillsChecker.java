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
import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.Checker;
import com.senither.hypixel.statistics.responses.SkillsResponse;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import javax.annotation.Nullable;

public class SkillsChecker extends Checker<SkillsResponse> {

    @Override
    public SkillsResponse checkUser(@Nullable PlayerReply playerReply, SkyBlockProfileReply profileReply, JsonObject member) {
        double mining = getDoubleFromObject(member, "experience_skill_mining");
        double foraging = getDoubleFromObject(member, "experience_skill_foraging");
        double enchanting = getDoubleFromObject(member, "experience_skill_enchanting");
        double farming = getDoubleFromObject(member, "experience_skill_farming");
        double combat = getDoubleFromObject(member, "experience_skill_combat");
        double fishing = getDoubleFromObject(member, "experience_skill_fishing");
        double alchemy = getDoubleFromObject(member, "experience_skill_alchemy");
        double taming = getDoubleFromObject(member, "experience_skill_taming");
        double carpentry = getDoubleFromObject(member, "experience_skill_carpentry");
        double runecrafting = getDoubleFromObject(member, "experience_skill_runecrafting");

        if (mining + foraging + enchanting + farming + combat + fishing + alchemy + taming == 0) {
            return playerReply == null
                ? new SkillsResponse(false, false)
                : checkUsingAchievements(playerReply);
        }

        return new SkillsResponse(true, true)
            .setMining(getSkillLevelFromExperience(mining, false), mining)
            .setForaging(getSkillLevelFromExperience(foraging, false), foraging)
            .setEnchanting(getSkillLevelFromExperience(enchanting, false), enchanting)
            .setFarming(getSkillLevelFromExperience(farming, false), farming)
            .setCombat(getSkillLevelFromExperience(combat, false), combat)
            .setFishing(getSkillLevelFromExperience(fishing, false), fishing)
            .setAlchemy(getSkillLevelFromExperience(alchemy, false), alchemy)
            .setTaming(getSkillLevelFromExperience(taming, false), taming)
            .setCarpentry(getSkillLevelFromExperience(carpentry, false), carpentry)
            .setRunecrafting(getSkillLevelFromExperience(runecrafting, true), runecrafting);
    }

    private SkillsResponse checkUsingAchievements(PlayerReply playerReply) {
        JsonObject achievements = playerReply.getPlayer().get("achievements").getAsJsonObject();

        double mining = getDoubleFromObject(achievements, "skyblock_excavator");
        double foraging = getDoubleFromObject(achievements, "skyblock_gatherer");
        double enchanting = getDoubleFromObject(achievements, "skyblock_augmentation");
        double farming = getDoubleFromObject(achievements, "skyblock_harvester");
        double combat = getDoubleFromObject(achievements, "skyblock_combat");
        double fishing = getDoubleFromObject(achievements, "skyblock_angler");
        double alchemy = getDoubleFromObject(achievements, "skyblock_concoctor");
        double taming = getDoubleFromObject(achievements, "skyblock_domesticator");

        if (mining + foraging + enchanting + farming + combat + fishing + alchemy + taming == 0) {
            return new SkillsResponse(false, false);
        }

        return new SkillsResponse(false, true)
            .setMining(mining, -1)
            .setForaging(foraging, -1)
            .setEnchanting(enchanting, -1)
            .setFarming(farming, -1)
            .setCombat(combat, -1)
            .setFishing(fishing, -1)
            .setAlchemy(alchemy, -1)
            .setTaming(taming, -1);
    }

    private double getSkillLevelFromExperience(double experience, boolean isRunecrafting) {
        int level = 0;
        for (int toRemove : isRunecrafting ? Constants.RUNECRAFTING_SKILL_EXPERIENCE : Constants.GENERAL_SKILL_EXPERIENCE) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return level;
    }
}
