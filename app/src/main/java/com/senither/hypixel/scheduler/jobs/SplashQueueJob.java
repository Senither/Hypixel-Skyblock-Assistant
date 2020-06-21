package com.senither.hypixel.scheduler.jobs;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.scheduler.Job;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.database.controller.PlayerDonationController;
import com.senither.hypixel.splash.SplashContainer;
import com.senither.hypixel.time.Carbon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class SplashQueueJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(SplashQueueJob.class);

    public SplashQueueJob(SkyblockAssistant app) {
        super(app, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            Iterator<SplashContainer> iterator = app.getSplashManager().getSplashes().iterator();
            while (iterator.hasNext()) {
                SplashContainer next = iterator.next();

                if (next.shouldUpdate()) {
                    app.getSplashManager().updateSplashFor(next);
                    next.setLastUpdatedAt(Carbon.now().getTimestamp());
                }

                if (next.isFinished()) {
                    iterator.remove();

                    handlePointsAssignments(next);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while processing the splash queue, error: {}", e.getMessage(), e);
        }
    }

    private void handlePointsAssignments(SplashContainer container) {
        GuildController.GuildEntry guild = GuildController.getGuildById(
            app.getDatabaseManager(), container.getDiscordId()
        );

        if (guild == null || !guild.isSplashTrackerEnabled() || !guild.isDonationsTrackerEnabled() || !guild.getSplashPoints()) {
            return;
        }

        PlayerDonationController.PlayerDonationEntry player = PlayerDonationController.getPlayerByUuid(
            app.getDatabaseManager(), container.getDiscordId(), container.getUserUuid()
        );

        if (player == null) {
            return;
        }

        player.setPoints(player.getPoints() + guild.getDonationPoints());

        try {
            app.getDatabaseManager().queryUpdate("UPDATE `donation_points` SET `points` = ? WHERE `discord_id` = ? AND `uuid` = ?",
                player.getPoints(), player.getDiscordId(), player.getUuid()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
