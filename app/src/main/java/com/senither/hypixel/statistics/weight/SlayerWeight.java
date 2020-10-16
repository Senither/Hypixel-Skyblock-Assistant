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

    public Weight calculateSkillWeight(double experience) {
        if (experience == 0) {
            return new Weight(0D, 0D);
        }

        if (experience <= 1000000) {
            return new Weight(experience / divider, 0D);
        }

        double base = 1000000 / divider;
        double remaining = experience - 1000000;
        double overflow = Math.pow(remaining / (divider * 1.5), 0.942);

        return new Weight(base + overflow, 0D);
    }
}
