package com.senither.hypixel.statistics.weight;

import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.SkillWeightRelationFunction;
import com.senither.hypixel.hypixel.SkyBlockSkill;
import com.senither.hypixel.statistics.responses.SkillsResponse;

public enum SkillWeight {

    /**
     * Maxes out mining at 850 points at level 50.
     */
    MINING(SkyBlockSkill.MINING, SkillsResponse::getMining, 1.232826, 259634),

    /**
     * Maxes out foraging at 850 points at level 50.
     */
    FORAGING(SkyBlockSkill.FORAGING, SkillsResponse::getForaging, 1.232826, 259634),

    /**
     * Maxes out enchanting at 250 points at level 50.
     */
    ENCHANTING(SkyBlockSkill.ENCHANTING, SkillsResponse::getEnchanting, 1.035905, 882758),

    /**
     * Maxes out farming at 1,000 points at level 50.
     */
    FARMING(SkyBlockSkill.FARMING, SkillsResponse::getFarming, 1.217848139, 220689),

    /**
     * Maxes out combat at 800 points at level 50.
     */
    COMBAT(SkyBlockSkill.COMBAT, SkillsResponse::getCombat, 1.22307, 275862),

    /**
     * Maxes out fishing at 2,500 points at level 50.
     */
    FISHING(SkyBlockSkill.FISHING, SkillsResponse::getFishing, 1.406418, 88274),

    /**
     * Maxes out alchemy at 200 points at level 50.
     */
    ALCHEMY(SkyBlockSkill.ALCHEMY, SkillsResponse::getAlchemy, 1.0, 1103448),

    /**
     * Maxes out taming at 500 points at level 50.
     */
    TAMING(SkyBlockSkill.TAMING, SkillsResponse::getTaming, 1.14744, 441379);

    private final SkyBlockSkill skillType;
    private final SkillWeightRelationFunction function;
    private final double exponent;
    private final double divider;

    SkillWeight(SkyBlockSkill skillType, SkillWeightRelationFunction function, double expo, double divider) {
        this.skillType = skillType;
        this.function = function;
        this.exponent = expo;
        this.divider = divider;
    }

    public SkillsResponse.SkillStat getSkillStatsRelation(SkillsResponse response) {
        return function.getWeight(response);
    }

    public Weight calculateSkillWeight(double experience) {
        double level = getSkillLevelFromExperience(experience);
        double base = Math.pow(level * 10, 0.5 + this.exponent + (level / 100)) / 1250;

        if (experience <= skillType.getMaxLevelExp()) {
            return new Weight(base, 0D);
        }

        return new Weight(base, Math.pow((experience - skillType.getMaxLevelExp()) / divider, 0.968));
    }

    private double getSkillLevelFromExperience(double experience) {
        int level = 0;
        for (int toRemove : Constants.GENERAL_SKILL_EXPERIENCE) {
            experience -= toRemove;
            if (experience < 0) {
                return Math.min(level + (1D - (experience * -1) / (double) toRemove), skillType.getMaxLevel());
            }
            level++;
        }
        return Math.min(level, skillType.getMaxLevel());
    }
}
