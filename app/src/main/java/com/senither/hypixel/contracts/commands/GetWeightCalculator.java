package com.senither.hypixel.contracts.commands;

import com.senither.hypixel.statistics.weight.Weight;

@FunctionalInterface
public interface GetWeightCalculator {

    Weight getWeight(double experience);
}
