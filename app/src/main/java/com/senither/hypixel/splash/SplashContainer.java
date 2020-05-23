package com.senither.hypixel.splash;

import com.senither.hypixel.time.Carbon;

public class SplashContainer {

    private final long id;
    private final long discordId;
    private final long userId;
    private final Carbon time;

    private String note;
    private long messageId;
    private long lastUpdatedAt;

    public SplashContainer(long id, long discordId, long userId, long messageId, Carbon time, String note) {
        this.id = id;
        this.discordId = discordId;
        this.userId = userId;
        this.messageId = messageId;
        this.time = time;
        this.note = note;

        lastUpdatedAt = Carbon.now().getTimestamp();
    }

    public long getId() {
        return id;
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

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public Carbon getTime() {
        return time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public boolean shouldUpdate() {
        long secondsSinceLastUpdate = Carbon.now().getTimestamp() - getLastUpdatedAt();
        long secondsLeft = time.diffInSeconds();

        return secondsLeft < 30
            || secondsLeft < 300 && secondsSinceLastUpdate > 25
            || secondsSinceLastUpdate > 60;
    }

    public boolean isFinished() {
        return time.isPast();
    }

    public boolean isEndingSoon() {
        return time.diffInSeconds() <= SplashManager.getEndingSoonTimer();
    }
}
