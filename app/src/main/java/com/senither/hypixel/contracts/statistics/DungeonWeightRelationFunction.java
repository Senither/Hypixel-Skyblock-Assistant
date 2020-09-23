package com.senither.hypixel.contracts.statistics;

import com.senither.hypixel.statistics.responses.DungeonResponse;

@FunctionalInterface
public interface DungeonWeightRelationFunction {

    CanCalculateWeight getWeight(DungeonResponse response);
}
