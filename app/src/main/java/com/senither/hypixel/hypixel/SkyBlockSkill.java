package com.senither.hypixel.hypixel;

public enum SkyBlockSkill {

    MINING(60),
    FORAGING(50),
    ENCHANTING(60),
    FARMING(60),
    COMBAT(60),
    FISHING(50),
    ALCHEMY(50),
    TAMING(50),
    CARPENTRY(50),
    RUNECRAFTING(25, 94300);

    private final int maxLevel;
    private final int maxLevelExp;

    SkyBlockSkill(int maxLevel) {
        this(maxLevel, maxLevel == 50 ? 55172425 : 111672425);
    }

    SkyBlockSkill(int maxLevel, int maxLevelExp) {
        this.maxLevel = maxLevel;
        this.maxLevelExp = maxLevelExp;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMaxLevelExp() {
        return maxLevelExp;
    }
}
