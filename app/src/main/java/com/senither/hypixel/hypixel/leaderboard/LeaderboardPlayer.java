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
import com.senither.hypixel.utils.NumberUtil;

import java.util.UUID;

public abstract class LeaderboardPlayer {

    protected UUID uuid;
    protected String guild_name;
    protected String username;
    // Average Stats
    protected double average_skill;
    protected double average_skill_progress;
    protected double total_slayer;
    // Slayer
    protected double revenant_xp;
    protected double tarantula_xp;
    protected double sven_xp;
    protected double enderman_xp;
    // Dungeons
    protected int secrets_found;
    protected double catacomb;
    protected double catacomb_xp;
    protected double healer;
    protected double healer_xp;
    protected double mage;
    protected double mage_xp;
    protected double berserk;
    protected double berserk_xp;
    protected double archer;
    protected double archer_xp;
    protected double tank;
    protected double tank_xp;
    // Skills
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
    protected double taming;
    protected double taming_xp;
    protected double carpentry;
    protected double carpentry_xp;
    protected double runecrafting;
    protected double runecrafting_xp;
    // Player Weight
    protected PlayerWeight raw_weight;

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

    public double getEndermanXp() {
        return enderman_xp;
    }

    public int getSecretsFound() {
        return secrets_found;
    }

    public double getCatacomb() {
        return catacomb;
    }

    public double getCatacombXp() {
        return catacomb_xp;
    }

    public double getHealer() {
        return healer;
    }

    public double getHealerXp() {
        return healer_xp;
    }

    public double getMage() {
        return mage;
    }

    public double getMageXp() {
        return mage_xp;
    }

    public double getBerserk() {
        return berserk;
    }

    public double getBerserkXp() {
        return berserk_xp;
    }

    public double getArcher() {
        return archer;
    }

    public double getArcherXp() {
        return archer_xp;
    }

    public double getTank() {
        return tank;
    }

    public double getTankXp() {
        return tank_xp;
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

    public double getTaming() {
        return taming;
    }

    public double getTamingXP() {
        return taming_xp <= 0 ? getExperienceForLevel(taming, false) : taming_xp;
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

    public double getTotalSkillExperience() {
        if (!isSkillsApiDisabled()) {
            return getMiningXP()
                + getForagingXP()
                + getEnchantingXP()
                + getFarmingXP()
                + getCombatXP()
                + getFishingXP()
                + getAlchemyXP()
                + getTamingXP();
        }

        return getExperienceForLevel(getMining(), false)
            + getExperienceForLevel(getForaging(), false)
            + getExperienceForLevel(getEnchanting(), false)
            + getExperienceForLevel(getFarming(), false)
            + getExperienceForLevel(getCombat(), false)
            + getExperienceForLevel(getFishing(), false)
            + getExperienceForLevel(getAlchemy(), false)
            + getExperienceForLevel(getTaming(), false);
    }

    public PlayerWeight getRawWeight() {
        return raw_weight;
    }

    private boolean isSkillsApiDisabled() {
        return getMiningXP() == -1
            && getForagingXP() == -1
            && getEnchantingXP() == -1
            && getFarmingXP() == -1
            && getCombatXP() == -1
            && getFishingXP() == -1
            && getAlchemyXP() == -1
            && getTamingXP() == -1;
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

    public class PlayerWeight extends WeightContainer {

        protected double total;
        protected SkillWeight skills;
        protected SlayerWeight slayers;
        protected DungeonWeight dungeons;

        public double getTotal() {
            return total;
        }

        public SkillWeight getSkills() {
            return skills;
        }

        public SlayerWeight getSlayers() {
            return slayers;
        }

        public DungeonWeight getDungeons() {
            return dungeons;
        }
    }

    public class SkillWeight {

        protected WeightContainer total;
        protected WeightContainer mining;
        protected WeightContainer foraging;
        protected WeightContainer enchanting;
        protected WeightContainer farming;
        protected WeightContainer combat;
        protected WeightContainer fishing;
        protected WeightContainer alchemy;
        protected WeightContainer taming;

        public WeightContainer getTotal() {
            return total;
        }

        public WeightContainer getMining() {
            return mining;
        }

        public WeightContainer getForaging() {
            return foraging;
        }

        public WeightContainer getEnchanting() {
            return enchanting;
        }

        public WeightContainer getFarming() {
            return farming;
        }

        public WeightContainer getCombat() {
            return combat;
        }

        public WeightContainer getFishing() {
            return fishing;
        }

        public WeightContainer getAlchemy() {
            return alchemy;
        }

        public WeightContainer getTaming() {
            return taming;
        }
    }

    public class SlayerWeight {

        protected WeightContainer total;
        protected WeightContainer revenant;
        protected WeightContainer tarantula;
        protected WeightContainer sven;
        protected WeightContainer enderman;

        public WeightContainer getTotal() {
            return total;
        }

        public WeightContainer getRevenant() {
            return revenant;
        }

        public WeightContainer getTarantula() {
            return tarantula;
        }

        public WeightContainer getSven() {
            return sven;
        }

        public WeightContainer getEnderman() {
            return sven;
        }
    }

    public class DungeonWeight {

        protected WeightContainer total;
        protected WeightContainer catacomb;
        protected WeightContainer healer;
        protected WeightContainer mage;
        protected WeightContainer berserk;
        protected WeightContainer archer;
        protected WeightContainer tank;

        public WeightContainer getTotal() {
            return total;
        }

        public WeightContainer getCatacomb() {
            return catacomb;
        }

        public WeightContainer getHealer() {
            return healer;
        }

        public WeightContainer getMage() {
            return mage;
        }

        public WeightContainer getBerserk() {
            return berserk;
        }

        public WeightContainer getArcher() {
            return archer;
        }

        public WeightContainer getTank() {
            return tank;
        }
    }

    public class WeightContainer {

        protected double weight;
        protected double overflow;

        public double getWeight() {
            return weight;
        }

        public double getOverflow() {
            return overflow;
        }

        public double getTotalWeight() {
            return getWeight() + getOverflow();
        }

        @Override
        public String toString() {
            if (overflow == 0) {
                return NumberUtil.formatNicelyWithDecimals(weight);
            }
            return NumberUtil.formatNicelyWithDecimals(weight)
                + " +" + NumberUtil.formatNicelyWithDecimals(overflow);
        }
    }
}
