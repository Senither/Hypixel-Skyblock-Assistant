package com.senither.hypixel.contracts.statistics;

import com.senither.hypixel.statistics.weight.Weight;

public interface CanCalculateWeight extends HasLevel {

    Weight calculateWeight();
}
