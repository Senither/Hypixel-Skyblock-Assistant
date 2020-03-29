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

import com.senither.hypixel.contracts.statistics.StatisticsResponse;

public class SkillsResponse extends StatisticsResponse {

    private final boolean hasData;

    private SkillStat mining = new SkillStat();
    private SkillStat foraging = new SkillStat();
    private SkillStat enchanting = new SkillStat();
    private SkillStat farming = new SkillStat();
    private SkillStat combat = new SkillStat();
    private SkillStat fishing = new SkillStat();
    private SkillStat alchemy = new SkillStat();
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
            getAlchemy().getLevel();

        if (combinedLevels <= 0) {
            return 0D;
        }
        return combinedLevels / 7D;
    }

    public double getAverageSkillLevelWithoutPorgress() {
        double combinedLevels = Math.floor(getMining().getLevel()) +
            Math.floor(getForaging().getLevel()) +
            Math.floor(getEnchanting().getLevel()) +
            Math.floor(getFarming().getLevel()) +
            Math.floor(getCombat().getLevel()) +
            Math.floor(getFishing().getLevel()) +
            Math.floor(getAlchemy().getLevel());

        if (combinedLevels <= 0) {
            return 0D;
        }
        return combinedLevels / 7D;
    }

    public class SkillStat {

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
    }
}
