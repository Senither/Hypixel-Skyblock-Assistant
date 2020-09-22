package com.senither.hypixel.statistics.weight;

import com.senither.hypixel.contracts.statistics.SlayerWeightRelationFunction;
import com.senither.hypixel.statistics.responses.SlayerResponse;

public enum SlayerWeight {

    REVENANT(SlayerResponse::getRevenant, 2208),
    TARANTULA(SlayerResponse::getTarantula, 2118),
    SVEN(SlayerResponse::getSven, 1962);

    private final SlayerWeightRelationFunction function;
    private final double divider;

    SlayerWeight(SlayerWeightRelationFunction function, double divider) {
        this.function = function;
        this.divider = divider;
    }

    public SlayerResponse.SlayerStat getSlayerStatsRelation(SlayerResponse response) {
        return function.getWeight(response);
    }

    public double calculateSkillWeight(double experience) {
        if (experience == 0) {
            return 0D;
        }
        return experience / divider;
    }
}
