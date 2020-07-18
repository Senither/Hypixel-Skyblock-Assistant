package com.senither.hypixel.scheduler.jobs;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.scheduler.Job;
import com.senither.hypixel.hypixel.Hypixel;

import java.util.concurrent.TimeUnit;

public class GarbageCollectorJob extends Job {

    public GarbageCollectorJob(SkyblockAssistant app) {
        super(app, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        synchronized (Hypixel.uuidToUsernameCache) {
            Hypixel.uuidToUsernameCache.cleanUp();
        }

        synchronized (Hypixel.usernameToUuidCache) {
            Hypixel.usernameToUuidCache.cleanUp();
        }

        synchronized (Hypixel.uuidToDiscordIdCache) {
            Hypixel.usernameToUuidCache.cleanUp();
        }

        synchronized (Hypixel.replyCache) {
            Hypixel.replyCache.cleanUp();
        }

        synchronized (Hypixel.responseCache) {
            Hypixel.responseCache.cleanUp();
        }
    }
}
