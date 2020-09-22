package com.senither.hypixel.contracts.statistics;

import com.senither.hypixel.statistics.responses.SkillsResponse;

@FunctionalInterface
public interface SkillWeightRelationFunction {

    SkillsResponse.SkillStat getWeight(SkillsResponse response);
}
