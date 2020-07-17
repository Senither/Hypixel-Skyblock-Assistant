package com.senither.hypixel.commands;

public class ThrottleContainer {

    private final int maxAttempts;
    private final int decaySeconds;

    public ThrottleContainer(int maxAttempts, int decaySeconds) {
        this.maxAttempts = maxAttempts;
        this.decaySeconds = decaySeconds;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getDecaySeconds() {
        return decaySeconds;
    }
}
