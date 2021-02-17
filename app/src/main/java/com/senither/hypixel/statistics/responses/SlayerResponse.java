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
import com.senither.hypixel.contracts.statistics.HasLevel;
import com.senither.hypixel.contracts.statistics.Jsonable;
import com.senither.hypixel.contracts.statistics.StatisticsResponse;
import com.senither.hypixel.statistics.weight.SlayerWeight;
import com.senither.hypixel.statistics.weight.Weight;

public class SlayerResponse extends StatisticsResponse implements Jsonable {

    private long totalCoinsSpent = 0;
    private long totalSlayerExperience = 0;

    private SlayerStat revenant = new SlayerStat(SlayerWeight.REVENANT);
    private SlayerStat tarantula = new SlayerStat(SlayerWeight.TARANTULA);
    private SlayerStat sven = new SlayerStat(SlayerWeight.SVEN);

    public SlayerResponse(boolean apiEnable) {
        super(apiEnable);
    }

    public long getTotalCoinsSpent() {
        return totalCoinsSpent;
    }

    public SlayerResponse setTotalCoinsSpent(long totalCoinsSpent) {
        this.totalCoinsSpent = totalCoinsSpent;

        return this;
    }

    public long getTotalSlayerExperience() {
        return totalSlayerExperience;
    }

    public SlayerResponse setTotalSlayerExperience(long totalSlayerExperience) {
        this.totalSlayerExperience = totalSlayerExperience;

        return this;
    }

    public SlayerStat getRevenant() {
        return revenant;
    }

    public SlayerResponse setRevenant(int experience, int tier1Kills, int tier2Kills, int tier3Kills, int tier4Kills) {
        this.revenant = new SlayerStat(SlayerWeight.REVENANT, experience, tier1Kills, tier2Kills, tier3Kills, tier4Kills);

        return this;
    }

    public SlayerStat getTarantula() {
        return tarantula;
    }

    public SlayerResponse setTarantula(int experience, int tier1Kills, int tier2Kills, int tier3Kills, int tier4Kills) {
        this.tarantula = new SlayerStat(SlayerWeight.TARANTULA, experience, tier1Kills, tier2Kills, tier3Kills, tier4Kills);

        return this;
    }

    public SlayerStat getSven() {
        return sven;
    }

    public SlayerResponse setSven(int experience, int tier1Kills, int tier2Kills, int tier3Kills, int tier4Kills) {
        this.sven = new SlayerStat(SlayerWeight.SVEN, experience, tier1Kills, tier2Kills, tier3Kills, tier4Kills);

        return this;
    }

    public Weight calculateTotalWeight() {
        Weight weight = new Weight(0D, 0D);

        for (SlayerWeight value : SlayerWeight.values()) {
            weight = weight.add(value.getSlayerStatsRelation(this).calculateWeight());
        }

        return weight;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("total_coins_spent", getTotalCoinsSpent());
        json.addProperty("total_experience", getTotalSlayerExperience());

        JsonObject bosses = new JsonObject();
        bosses.add("revenant", revenant.toJson());
        bosses.add("tarantula", tarantula.toJson());
        bosses.add("sven", sven.toJson());
        json.add("bosses", bosses);

        return json;
    }

    public class SlayerStat implements Jsonable, HasLevel {

        private final SlayerWeight weight;
        private final int experience;
        private final int tier1Kills;
        private final int tier2Kills;
        private final int tier3Kills;
        private final int tier4Kills;

        SlayerStat(SlayerWeight weight) {
            this.weight = weight;

            experience = 0;
            tier1Kills = 0;
            tier2Kills = 0;
            tier3Kills = 0;
            tier4Kills = 0;
        }

        SlayerStat(SlayerWeight weight, int experience, int tier1Kills, int tier2Kills, int tier3Kills, int tier4Kills) {
            this.weight = weight;
            this.experience = experience;
            this.tier1Kills = tier1Kills;
            this.tier2Kills = tier2Kills;
            this.tier3Kills = tier3Kills;
            this.tier4Kills = tier4Kills;
        }

        @Override
        public double getLevel() {
            for (int level = 0; level < Constants.SLAYER_EXPERIENCE.size(); level++) {
                double requirement = Constants.SLAYER_EXPERIENCE.asList().get(level);
                if (this.experience < requirement) {
                    double lastRequirement = level == 0 ? 0D : Constants.SLAYER_EXPERIENCE.asList().get(level - 1);
                    return level + (this.experience - lastRequirement) / (requirement - lastRequirement);
                }
            }
            return 9D;
        }

        @Override
        public double getExperience() {
            return experience;
        }

        public int getTier1Kills() {
            return tier1Kills;
        }

        public int getTier2Kills() {
            return tier2Kills;
        }

        public int getTier3Kills() {
            return tier3Kills;
        }

        public int getTier4Kills() {
            return tier4Kills;
        }

        public Weight calculateWeight() {
            return weight.calculateSkillWeight(experience);
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("level", getLevel());
            json.addProperty("experience", getExperience());

            JsonObject kills = new JsonObject();
            kills.addProperty("tier_1", getTier1Kills());
            kills.addProperty("tier_2", getTier2Kills());
            kills.addProperty("tier_3", getTier3Kills());
            kills.addProperty("tier_4", getTier4Kills());
            json.add("kills", kills);

            return json;
        }
    }
}
