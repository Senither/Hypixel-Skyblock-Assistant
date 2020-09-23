package com.senither.hypixel.statistics.weight;

import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.CanCalculateWeight;
import com.senither.hypixel.contracts.statistics.DungeonWeightRelationFunction;
import com.senither.hypixel.statistics.responses.DungeonResponse;

public enum DungeonWeight {

    /**
     * Maxes out Catacomb at 1,000 points at level 50.
     */
    CATACOMB(response -> response.getDungeonFromType(DungeonResponse.DungeonType.CATACOMBS), 1000),

    /**
     * Maxes out Healer at 500 points at level 50.
     */
    HEALER(response -> response.getClassFromType(DungeonResponse.DungeonClassType.HEALER), 500),

    /**
     * Maxes out Mage at 500 points at level 50.
     */
    MAGE(response -> response.getClassFromType(DungeonResponse.DungeonClassType.MAGE), 500),

    /**
     * Maxes out Berserk at 500 points at level 50.
     */
    BERSERK(response -> response.getClassFromType(DungeonResponse.DungeonClassType.BERSERK), 500),

    /**
     * Maxes out Archer at 500 points at level 50.
     */
    ARCHER(response -> response.getClassFromType(DungeonResponse.DungeonClassType.ARCHER), 500),

    /**
     * Maxes out Tank at 500 points at level 50.
     */
    TANK(response -> response.getClassFromType(DungeonResponse.DungeonClassType.TANK), 500);

    private static final double level50Exp = 569809640;

    private final DungeonWeightRelationFunction function;
    private final double maxPoints;

    DungeonWeight(DungeonWeightRelationFunction function, double maxPoints) {
        this.function = function;
        this.maxPoints = maxPoints;
    }

    public CanCalculateWeight getCalculatorFromDungeon(DungeonResponse response) {
        return function.getWeight(response);
    }

    public Weight calculateWeight(double experience) {
        double level = getLevelFromExperience(experience);
        double base = maxPoints * ((level * 2) / 100);

        if (experience <= level50Exp) {
            return new Weight(base, 0D);
        }

        double remaining = experience - level50Exp;
        double splitter = (level50Exp / maxPoints) * 4;

        return new Weight(base, Math.pow(remaining / splitter, 0.968));
    }

    private double getLevelFromExperience(double experience) {
        int level = 0;
        for (int toRemove : Constants.DUNGEON_EXPERIENCE) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return level;
    }
}
