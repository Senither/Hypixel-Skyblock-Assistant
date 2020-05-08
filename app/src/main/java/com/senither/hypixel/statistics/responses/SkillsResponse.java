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

package com.senither.hypixel.statistics.responses;

import com.google.gson.JsonObject;
import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.Jsonable;
import com.senither.hypixel.contracts.statistics.StatisticsResponse;

public class SkillsResponse extends StatisticsResponse implements Jsonable {

    private final boolean hasData;

    private SkillStat mining = new SkillStat();
    private SkillStat foraging = new SkillStat();
    private SkillStat enchanting = new SkillStat();
    private SkillStat farming = new SkillStat();
    private SkillStat combat = new SkillStat();
    private SkillStat fishing = new SkillStat();
    private SkillStat alchemy = new SkillStat();
    private SkillStat taming = new SkillStat();
    private SkillStat carpentry = new SkillStat();
    private SkillStat runecrafting = new SkillStat();

    public SkillsResponse(boolean apiEnable, boolean hasData) {
        super(apiEnable);

        this.hasData = hasData;
    }

    public boolean hasData() {
        return hasData;
    }

    public SkillStat getMining() {
        return mining;
    }

    public SkillsResponse setMining(double level, double experience) {
        this.mining = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getForaging() {
        return foraging;
    }

    public SkillsResponse setForaging(double level, double experience) {
        this.foraging = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getEnchanting() {
        return enchanting;
    }

    public SkillsResponse setEnchanting(double level, double experience) {
        this.enchanting = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getFarming() {
        return farming;
    }

    public SkillsResponse setFarming(double level, double experience) {
        this.farming = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getCombat() {
        return combat;
    }

    public SkillsResponse setCombat(double level, double experience) {
        this.combat = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getFishing() {
        return fishing;
    }

    public SkillsResponse setFishing(double level, double experience) {
        this.fishing = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getAlchemy() {
        return alchemy;
    }

    public SkillsResponse setAlchemy(double level, double experience) {
        this.alchemy = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getTaming() {
        return taming;
    }

    public SkillsResponse setTaming(double level, double experience) {
        this.taming = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getCarpentry() {
        return carpentry;
    }

    public SkillsResponse setCarpentry(double level, double experience) {
        this.carpentry = new SkillStat(level, experience);

        return this;
    }

    public SkillStat getRunecrafting() {
        return runecrafting;
    }

    public SkillsResponse setRunecrafting(double level, double experience) {
        this.runecrafting = new SkillStat(level, experience);

        return this;
    }

    public double getAverageSkillLevel() {
        double combinedLevels = getMining().getLevel() +
            getForaging().getLevel() +
            getEnchanting().getLevel() +
            getFarming().getLevel() +
            getCombat().getLevel() +
            getFishing().getLevel() +
            getAlchemy().getLevel() +
            getTaming().getLevel();

        if (combinedLevels <= 0) {
            return 0D;
        }
        return combinedLevels / 8D;
    }

    public double getAverageSkillLevelWithoutPorgress() {
        double combinedLevels = Math.floor(getMining().getLevel()) +
            Math.floor(getForaging().getLevel()) +
            Math.floor(getEnchanting().getLevel()) +
            Math.floor(getFarming().getLevel()) +
            Math.floor(getCombat().getLevel()) +
            Math.floor(getFishing().getLevel()) +
            Math.floor(getAlchemy().getLevel()) +
            Math.floor(getTaming().getLevel());

        if (combinedLevels <= 0) {
            return 0D;
        }
        return combinedLevels / 8D;
    }

    public double getTotalSkillExperience() {
        double combinedExperience = getMining().getExperience() +
            getForaging().getExperience() +
            getEnchanting().getExperience() +
            getFarming().getExperience() +
            getCombat().getExperience() +
            getFishing().getExperience() +
            getAlchemy().getExperience() +
            getTaming().getExperience();

        if (combinedExperience <= 0) {
            return 0D;
        }
        return combinedExperience;
    }

    public double getTotalEffectiveSkillExperience() {
        if (hasData()) {
            return getTotalSkillExperience();
        }

        double combinedExperience = getExperienceForLevel(getMining().getLevel()) +
            getExperienceForLevel(getForaging().getLevel()) +
            getExperienceForLevel(getEnchanting().getLevel()) +
            getExperienceForLevel(getFarming().getLevel()) +
            getExperienceForLevel(getCombat().getLevel()) +
            getExperienceForLevel(getFishing().getLevel()) +
            getExperienceForLevel(getAlchemy().getLevel()) +
            getExperienceForLevel(getTaming().getLevel());

        if (combinedExperience <= 0) {
            return 0D;
        }
        return combinedExperience;
    }

    private double getExperienceForLevel(double level) {
        double totalRequiredExperience = 0;
        for (int i = 0; i < Math.min(level, Constants.GENERAL_SKILL_EXPERIENCE.size()); i++) {
            totalRequiredExperience += Constants.GENERAL_SKILL_EXPERIENCE.asList().get(i);
        }
        return totalRequiredExperience;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("average_skills_progress", getAverageSkillLevel());
        json.addProperty("average_skills", getAverageSkillLevelWithoutPorgress());

        JsonObject skills = new JsonObject();
        skills.add("mining", getMining().toJson());
        skills.add("foraging", getForaging().toJson());
        skills.add("enchanting", getEnchanting().toJson());
        skills.add("farming", getFarming().toJson());
        skills.add("combat", getCombat().toJson());
        skills.add("fishing", getFishing().toJson());
        skills.add("alchemy", getAlchemy().toJson());
        skills.add("taming", getTaming().toJson());
        skills.add("carpentry", getCarpentry().toJson());
        skills.add("runecrafting", getRunecrafting().toJson());
        json.add("skills", skills);

        return json;
    }

    public class SkillStat implements Jsonable {

        private final double level;
        private final double experience;

        SkillStat() {
            this.level = -1;
            this.experience = -1;
        }

        SkillStat(double level, double experience) {
            this.level = level;
            this.experience = experience;
        }

        public double getLevel() {
            return level;
        }

        public double getExperience() {
            return experience;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();

            json.addProperty("level", level);
            json.addProperty("experience", experience);

            return json;
        }
    }
}
