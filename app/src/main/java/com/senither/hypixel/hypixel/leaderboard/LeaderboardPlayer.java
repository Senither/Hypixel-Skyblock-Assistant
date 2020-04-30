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

package com.senither.hypixel.hypixel.leaderboard;

import com.google.common.collect.ImmutableMultiset;
import com.senither.hypixel.Constants;

import java.util.UUID;

public abstract class LeaderboardPlayer {

    protected UUID uuid;
    protected String guild_name;
    protected String username;
    protected double average_skill;
    protected double average_skill_progress;
    protected double total_slayer;
    protected double revenant_xp;
    protected double tarantula_xp;
    protected double sven_xp;
    protected double mining;
    protected double mining_xp;
    protected double foraging;
    protected double foraging_xp;
    protected double enchanting;
    protected double enchanting_xp;
    protected double farming;
    protected double farming_xp;
    protected double combat;
    protected double combat_xp;
    protected double fishing;
    protected double fishing_xp;
    protected double alchemy;
    protected double alchemy_xp;
    protected double carpentry;
    protected double carpentry_xp;
    protected double runecrafting;
    protected double runecrafting_xp;

    public UUID getUuid() {
        return uuid;
    }

    public String getGuildName() {
        return guild_name;
    }

    public String getUsername() {
        return username;
    }

    public double getAverageSkill() {
        return average_skill;
    }

    public double getAverageSkillProgress() {
        return average_skill_progress;
    }

    public double getTotalSlayer() {
        return total_slayer;
    }

    public double getRevenantXP() {
        return revenant_xp;
    }

    public double getTarantulaXP() {
        return tarantula_xp;
    }

    public double getSvenXP() {
        return sven_xp;
    }

    public double getMining() {
        return mining;
    }

    public double getMiningXP() {
        return mining_xp <= 0 ? getExperienceForLevel(mining, false) : mining_xp;
    }

    public double getForaging() {
        return foraging;
    }

    public double getForagingXP() {
        return foraging_xp <= 0 ? getExperienceForLevel(foraging, false) : foraging_xp;
    }

    public double getEnchanting() {
        return enchanting;
    }

    public double getEnchantingXP() {
        return enchanting_xp <= 0 ? getExperienceForLevel(enchanting, false) : enchanting_xp;
    }

    public double getFarming() {
        return farming;
    }

    public double getFarmingXP() {
        return farming_xp <= 0 ? getExperienceForLevel(farming, false) : farming_xp;
    }

    public double getCombat() {
        return combat;
    }

    public double getCombatXP() {
        return combat_xp <= 0 ? getExperienceForLevel(combat, false) : combat_xp;
    }

    public double getFishing() {
        return fishing;
    }

    public double getFishingXP() {
        return fishing_xp <= 0 ? getExperienceForLevel(fishing, false) : fishing_xp;
    }

    public double getAlchemy() {
        return alchemy;
    }

    public double getAlchemyXP() {
        return alchemy_xp <= 0 ? getExperienceForLevel(alchemy, false) : alchemy_xp;
    }

    public double getCarpentry() {
        return carpentry;
    }

    public double getCarpentryXP() {
        return carpentry_xp <= 0 ? getExperienceForLevel(alchemy, false) : carpentry_xp;
    }

    public double getRunecrafting() {
        return runecrafting;
    }

    public double getRunecraftingXP() {
        return runecrafting_xp <= 0 ? getExperienceForLevel(runecrafting, true) : runecrafting_xp;
    }

    private double getExperienceForLevel(double level, boolean isRunecrafting) {
        double totalRequiredExperience = 0;
        ImmutableMultiset<Integer> experienceLevels = isRunecrafting
            ? Constants.RUNECRAFTING_SKILL_EXPERIENCE
            : Constants.GENERAL_SKILL_EXPERIENCE;

        for (int i = 0; i < Math.min(level, experienceLevels.size()); i++) {
            totalRequiredExperience += experienceLevels.asList().get(i);
        }
        return totalRequiredExperience;
    }
}
