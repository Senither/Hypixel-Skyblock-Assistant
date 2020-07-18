/*
 * Copyright (c) 2020.
 *
 * This file is part of Hypixel Skyblock Assistant.
 *
 * Hypixel Guild Synchronizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypixel Guild Synchronizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hypixel Guild Synchronizer.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.senither.hypixel.metrics;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.senither.hypixel.commands.middlewares.ThrottleMiddleware;
import com.senither.hypixel.commands.middlewares.VerificationMiddleware;
import com.senither.hypixel.commands.misc.BotStatsCommand;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.hypixel.Hypixel;
import com.senither.hypixel.servlet.routes.GetGuildRoute;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.guava.cache.CacheMetricsCollector;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.logback.InstrumentedAppender;
import net.dv8tion.jda.api.events.Event;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Set;

public class Metrics {

    // ################################################################################
    // ##                              JDA Stats
    // ################################################################################

    public static final Counter jdaEvents = Counter.build()
        .name("skyblock_assistant_jda_events_received_total")
        .help("All events that JDA provides us with by class")
        .labelNames("class") // GuildJoinedEvent, MessageReceivedEvent, ReconnectEvent etc
        .register();

    public static final Gauge memoryTotal = Gauge.build()
        .name("skyblock_assistant_memory_total")
        .help("Total number bytes of memory dedicated to the app")
        .register();

    public static final Gauge memoryUsed = Gauge.build()
        .name("skyblock_assistant_memory_used")
        .help("Total number bytes used in memory for the app")
        .register();

    // ################################################################################
    // ##                         SkyBlock Assistant Stats
    // ################################################################################

    public static final Gauge uptime = Gauge.build()
        .name("skyblock_assistant_uptime")
        .help("Total number of seconds the bot has been online for")
        .labelNames("type")
        .register();

    public static final Gauge guilds = Gauge.build()
        .name("skyblock_assistant_guilds_total")
        .help("Total number of guilds the bot is in")
        .register();

    public static final Gauge channels = Gauge.build()
        .name("skyblock_assistant_channels_total")
        .help("Total number of channels the bot is in")
        .labelNames("type")
        .register();

    public static final Gauge geoTracker = Gauge.build()
        .name("skyblock_assistant_geo_tracker_total")
        .help("Total number of guilds split up by geographic location")
        .labelNames("region")
        .register();

    public static final Gauge websocketHeartbeat = Gauge.build()
        .name("skyblock_assistant_shard_websocket_heartbeat")
        .help("Websocket heartbeat in milliseconds for each shard")
        .labelNames("shard")
        .register();

    public static final Counter commandsReceived = Counter.build()
        .name("skyblock_assistant_commands_received_total")
        .help("Total received commands. Some of these might get ratelimited.")
        .labelNames("class")
        .register();

    public static final Counter commandsExecuted = Counter.build()
        .name("skyblock_assistant_commands_executed_total")
        .help("Total executed commands by class")
        .labelNames("class")
        .register();

    public static final Counter commandsExecutedByGuild = Counter.build()
        .name("skyblock_assistant_guild_commands_executed_total")
        .help("Total executed commands by class")
        .labelNames("name")
        .register();

    public static final Counter commandsRatelimited = Counter.build()
        .name("skyblock_assistant_commands_ratelimited_total")
        .help("Total ratelimited commands")
        .labelNames("class")
        .register();

    public static final Histogram executionTime = Histogram.build() // commands execution time, excluding ratelimited ones
        .name("skyblock_assistant_command_execution_duration_seconds")
        .help("Command execution time, excluding handling ratelimited commands.")
        .labelNames("class")
        .register();

    public static final Counter commandExceptions = Counter.build()
        .name("skyblock_assistant_commands_exceptions_total")
        .help("Total uncaught exceptions thrown by command invocation")
        .labelNames("class") // class of the exception
        .register();

    public static final Counter databaseQueries = Counter.build()
        .name("skyblock_assistant_database_queries")
        .help("Total prepared statements created for the given type")
        .labelNames("type")
        .register();

    public static final Gauge blacklist = Gauge.build()
        .name("skyblock_assistant_blacklist_current")
        .help("The amount of servers and users that are currently on the blacklist")
        .register();

    private static boolean isSetup = false;

    public static void setup() {
        if (isSetup) {
            throw new IllegalStateException("The metrics has already been setup!");
        }

        uptime.labels("static").set(System.currentTimeMillis());

        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
        final InstrumentedAppender prometheusAppender = new InstrumentedAppender();
        prometheusAppender.setContext(root.getLoggerContext());
        prometheusAppender.start();
        root.addAppender(prometheusAppender);

        // JVM (hotspot) metrics
        DefaultExports.initialize();
        Metrics.initializeEventMetrics();

        CacheMetricsCollector cacheMetrics = new CacheMetricsCollector().register();
        cacheMetrics.addCache("bot-stats", BotStatsCommand.cache);
        cacheMetrics.addCache("levels", BotStatsCommand.cache);
        cacheMetrics.addCache("api-guilds", GetGuildRoute.guildCache);
        cacheMetrics.addCache("username-to-uuid", Hypixel.usernameToUuidCache);
        cacheMetrics.addCache("uuid-to-discord-id", Hypixel.uuidToDiscordIdCache);
        cacheMetrics.addCache("uuid-to-username", Hypixel.uuidToUsernameCache);
        cacheMetrics.addCache("reply", Hypixel.replyCache);
        cacheMetrics.addCache("response", Hypixel.responseCache);
        cacheMetrics.addCache("verify", VerificationMiddleware.cache);
        cacheMetrics.addCache("database-guild", GuildController.cache);
        cacheMetrics.addCache("discord-id-to-username", Command.discordIdToUsernameCache);
        cacheMetrics.addCache("discord-id-to-uuid", Command.discordIdToUuidCache);
        cacheMetrics.addCache("throttle-commands", ThrottleMiddleware.cache);

        isSetup = true;
    }

    private static void initializeEventMetrics() {
        Set<Class<? extends Event>> types = new Reflections("net.dv8tion.jda.core.events")
            .getSubTypesOf(Event.class);

        for (Class<? extends Event> type : types) {
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            jdaEvents.labels(type.getSimpleName()).inc(0D);
        }
    }
}
