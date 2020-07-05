package com.senither.hypixel.splash;

import com.senither.hypixel.time.Carbon;

import java.util.UUID;

public class SplashContainer {

    private final long id;
    private final long discordId;
    private final UUID userUuid;
    private final Carbon time;

    private String note;
    private long messageId;
    private long lastUpdatedAt;
    private boolean notifiedEveryone;

    public SplashContainer(long id, long discordId, UUID userUuid, long messageId, Carbon time, String note) {
        this.id = id;
        this.discordId = discordId;
        this.userUuid = userUuid;
        this.messageId = messageId;
        this.time = time;
        this.note = note;

        notifiedEveryone = time.diffInSeconds(Carbon.now()) <= SplashManager.getEndingSoonTimer();
        lastUpdatedAt = Carbon.now().getTimestamp();
    }

    public long getId() {
        return id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public UUID getUserUuid() {
        return userUuid;
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

    public void setNotifiedEveryone(boolean notifiedEveryone) {
        this.notifiedEveryone = notifiedEveryone;
    }

    public boolean hasNotifiedEveryone() {
        return notifiedEveryone;
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
