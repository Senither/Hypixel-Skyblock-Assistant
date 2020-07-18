package com.senither.hypixel.scheduler.jobs;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.blacklist.BlacklistEntity;
import com.senither.hypixel.contracts.scheduler.Job;
import com.senither.hypixel.metrics.Metrics;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class SyncMetricsJob extends Job {

    public SyncMetricsJob(SkyblockAssistant app) {
        super(app, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        jdaMetrics();
        uptimeMetrics();
        blacklistMetrics();
    }

    private void blacklistMetrics() {
        if (app.getBlacklist() == null) {
            return;
        }

        Metrics.blacklist.set(
            app.getBlacklist().getBlacklistEntities().stream()
                .filter(BlacklistEntity::isBlacklisted)
                .count()
        );
    }

    private void uptimeMetrics() {
        Metrics.uptime.labels("dynamic").set(ManagementFactory.getRuntimeMXBean().getUptime());
    }

    private void jdaMetrics() {
        Metrics.memoryTotal.set(Runtime.getRuntime().totalMemory());
        Metrics.memoryUsed.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        if (app.getShardManager() == null) {
            return;
        }

        Metrics.guilds.set(app.getShardManager().getGuilds().size());
        Metrics.channels.labels("text").set(app.getShardManager().getTextChannels().size());
        Metrics.channels.labels("voice").set(app.getShardManager().getVoiceChannels().size());

        for (Region region : Region.values()) {
            Metrics.geoTracker.labels(region.getName()).set(0);
        }

        for (JDA shard : app.getShardManager().getShards()) {
            Metrics.websocketHeartbeat.labels("Shard " + shard.getShardInfo().getShardId()).set(shard.getGatewayPing());

            for (Guild guild : shard.getGuilds()) {
                Metrics.geoTracker.labels(guild.getRegion().getName()).inc();
            }
        }
    }
}
