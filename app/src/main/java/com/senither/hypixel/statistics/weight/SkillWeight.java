package com.senither.hypixel.statistics.weight;

import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.SkillWeightRelationFunction;
import com.senither.hypixel.statistics.responses.SkillsResponse;

public enum SkillWeight {

    /**
     * Maxes out mining at 850 points at level 50.
     */
    MINING(SkillsResponse::getMining, 1.232826, 259634),

    /**
     * Maxes out foraging at 850 points at level 50.
     */
    FORAGING(SkillsResponse::getForaging, 1.232826, 259634),

    /**
     * Maxes out enchanting at 250 points at level 50.
     */
    ENCHANTING(SkillsResponse::getEnchanting, 1.035905, 882758),

    /**
     * Maxes out farming at 1,000 points at level 50.
     */
    FARMING(SkillsResponse::getFarming, 1.258976, 220689),

    /**
     * Maxes out taming at 800 points at level 50.
     */
    COMBAT(SkillsResponse::getCombat, 1.22307, 275862),

    /**
     * Maxes out fishing at 2,500 points at level 50.
     */
    FISHING(SkillsResponse::getFishing, 1.406418, 88274),

    /**
     * Maxes out alchemy at 200 points at level 50.
     */
    ALCHEMY(SkillsResponse::getAlchemy, 1.0, 1103448),

    /**
     * Maxes out taming at 800 points at level 50.
     */
    TAMING(SkillsResponse::getTaming, 1.22307, 275862);

    private static final double level50Exp = 55172425;

    private final SkillWeightRelationFunction function;
    private final double exponent;
    private final double divider;

    SkillWeight(SkillWeightRelationFunction function, double expo, double divider) {
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

        if (experience <= level50Exp) {
            return new Weight(base, 0D);
        }
        return new Weight(base, (experience - level50Exp) / divider);
    }

    private double getSkillLevelFromExperience(double experience) {
        int level = 0;
        for (int toRemove : Constants.GENERAL_SKILL_EXPERIENCE) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return level;
    }
}
