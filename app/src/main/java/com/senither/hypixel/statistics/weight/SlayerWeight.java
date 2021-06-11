package com.senither.hypixel.statistics.weight;

import com.senither.hypixel.contracts.statistics.SlayerWeightRelationFunction;
import com.senither.hypixel.statistics.responses.SlayerResponse;

public enum SlayerWeight {

    REVENANT(SlayerResponse::getRevenant, 2208, .15),
    TARANTULA(SlayerResponse::getTarantula, 2118, .08),
    SVEN(SlayerResponse::getSven, 1962, .015),
    ENDERMAN(SlayerResponse::getEnderman, 1430, .017);

    private final SlayerWeightRelationFunction function;
    private final double divider;
    private final double modifier;

    SlayerWeight(SlayerWeightRelationFunction function, double divider, double modifier) {
        this.function = function;
        this.divider = divider;
        this.modifier = modifier;
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

        double modifier = this.modifier;
        double overflow = 0;

        while (remaining > 0) {
            double left = Math.min(remaining, 1000000);

            overflow += Math.pow(left / (divider * (1.5 + modifier)), 0.942);
            remaining -= left;
            modifier += this.modifier;
        }

        return new Weight(base + overflow, 0D);
    }
}
