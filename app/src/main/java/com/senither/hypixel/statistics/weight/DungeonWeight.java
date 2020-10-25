package com.senither.hypixel.statistics.weight;

import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.CanCalculateWeight;
import com.senither.hypixel.contracts.statistics.DungeonWeightRelationFunction;
import com.senither.hypixel.statistics.responses.DungeonResponse;

public enum DungeonWeight {

    /**
     * Maxes out Catacomb at 6,500 points at level 50.
     */
    CATACOMB(response -> response.getDungeonFromType(DungeonResponse.DungeonType.CATACOMBS), 6500),

    /**
     * Maxes out Healer at 200 points at level 50.
     */
    HEALER(response -> response.getClassFromType(DungeonResponse.DungeonClassType.HEALER), 200),

    /**
     * Maxes out Mage at 200 points at level 50.
     */
    MAGE(response -> response.getClassFromType(DungeonResponse.DungeonClassType.MAGE), 200),

    /**
     * Maxes out Berserk at 200 points at level 50.
     */
    BERSERK(response -> response.getClassFromType(DungeonResponse.DungeonClassType.BERSERK), 200),

    /**
     * Maxes out Archer at 200 points at level 50.
     */
    ARCHER(response -> response.getClassFromType(DungeonResponse.DungeonClassType.ARCHER), 200),

    /**
     * Maxes out Tank at 200 points at level 50.
     */
    TANK(response -> response.getClassFromType(DungeonResponse.DungeonClassType.TANK), 200);

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
        double base = Math.pow(level * 10, 3) * (this.maxPoints / 100000) / 1250;

        if (experience <= level50Exp) {
            return new Weight(base, 0D);
        }

        return new Weight(base, Math.pow((experience - level50Exp) / (4 * level50Exp / maxPoints), 0.968));
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
