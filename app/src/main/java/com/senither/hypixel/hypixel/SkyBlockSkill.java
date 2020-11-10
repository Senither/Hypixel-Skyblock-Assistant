package com.senither.hypixel.hypixel;

public enum SkyBlockSkill {

    MINING(50),
    FORAGING(50),
    ENCHANTING(50),
    FARMING(60),
    COMBAT(50),
    FISHING(50),
    ALCHEMY(50),
    TAMING(50),
    CARPENTRY(50),
    RUNECRAFTING(25);

    private final int maxLevel;

    SkyBlockSkill(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
