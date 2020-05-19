package com.senither.hypixel.splash;

import com.senither.hypixel.time.Carbon;

public class SplashContainer {

    private final long discordId;
    private final long userId;
    private final long messageId;
    private final Carbon time;
    private final String note;

    private long lastUpdatedAt;

    public SplashContainer(long discordId, long userId, long messageId, Carbon time, String note) {
        this.discordId = discordId;
        this.userId = userId;
        this.messageId = messageId;
        this.time = time;
        this.note = note;

        lastUpdatedAt = Carbon.now().getTimestamp();
    }

    public long getDiscordId() {
        return discordId;
    }

    public long getUserId() {
        return userId;
    }

    public long getMessageId() {
        return messageId;
    }

    public Carbon getTime() {
        return time;
    }

    public String getNote() {
        return note;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public boolean shouldUpdate() {
        Carbon now = Carbon.now();

        long secondsSinceLastUpdate = now.getTimestamp() - getLastUpdatedAt();
        long secondsLeft = time.diffInSeconds(now);

        return secondsLeft < 30
            || secondsLeft < 60 && secondsSinceLastUpdate > 25
            || secondsSinceLastUpdate > 60;
    }

    public boolean isFinished() {
        return time.isPast();
    }
}
