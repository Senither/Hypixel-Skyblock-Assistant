package com.senither.hypixel.contracts.statistics;

import com.senither.hypixel.statistics.responses.SlayerResponse;

@FunctionalInterface
public interface SlayerWeightRelationFunction {

    SlayerResponse.SlayerStat getWeight(SlayerResponse response);
}
