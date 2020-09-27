package com.senither.hypixel.contracts.commands;

import com.senither.hypixel.contracts.statistics.StatisticsResponse;

@FunctionalInterface
public interface SetCalculatableSkill<T extends StatisticsResponse> {

    T setCalculateableSkill(T response, double level, double experience);
}
