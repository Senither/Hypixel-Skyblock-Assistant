package com.senither.hypixel.contracts.statistics;

public interface HasLevel {

    /**
     * Gets the skill level calculated from the total XP.
     *
     * @return The calculated skill level.
     */
    double getLevel();

    /**
     * The total experience the user has in the skill.
     *
     * @return The total experience.
     */
    double getExperience();
}
