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

package com.senither.hypixel.commands.misc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.metrics.MetricType;
import com.senither.hypixel.metrics.Metrics;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BotStatsCommand extends Command {

    private static final Cache<String, Long> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    public BotStatsCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Bot Statistics";
    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList(
            "Gets some statistics about the bot."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command` - Displays stats about the bot."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("botstats", "bstats");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        MessageFactory.makeInfo(event.getMessage(), "The bot has been online for :time!")
            .set("time", Constants.STARTED_BOT_AT.diffForHumans(true))
            .setTitle("Bot Statistics")
            .addField("DB Queries ran", formatDynamicValue(event, Metrics.getValue(MetricType.DB_QUERIES_RAN)), true)
            .addField("Commands Ran", formatDynamicValue(event, Metrics.getValue(MetricType.COMMANDS_RAN)), true)
            .addField("Messages Received", formatDynamicValue(event, Metrics.getValue(MetricType.MESSAGES_RECEIVED)), true)
            .addField("Cached UUIDs", NumberUtil.formatNicely(getTotalForType("uuids")), true)
            .addField("Cached Players", NumberUtil.formatNicely(getTotalForType("players")), true)
            .addField("Cached Profiles", NumberUtil.formatNicely(getTotalForType("profiles")), true)
            .setFooter(String.format("There are %s verified users with the bot.",
                NumberUtil.formatNicely(getTotalVerifiedPlayers())
            ))
            .setTimestamp(Carbon.now().getTime().toInstant())
            .queue();
    }

    private String formatDynamicValue(MessageReceivedEvent event, long rawValue) {
        double value = rawValue / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / 1000D);

        return MessageFactory.makeInfo(event.getMessage(), ":value (:sub per :unit)")
            .set("value", NumberUtil.formatNicely(rawValue))
            .set("sub", NumberUtil.formatNicelyWithDecimals(value < 1.5D ? value * 60D : value))
            .set("unit", value < 1.5D ? "min" : "sec")
            .toString();
    }

    private long getTotalForType(String type) {
        try {
            return cache.get(type, () -> {
                try {
                    return app.getDatabaseManager().query(String.format(
                        "SELECT COUNT(*) AS total FROM `%s`;",
                        type
                    )).first().getLong("total");
                } catch (Exception ignored) {
                    return 0L;
                }
            });
        } catch (ExecutionException e) {
            return 0L;
        }
    }

    private int getTotalVerifiedPlayers() {
        try {
            return app.getDatabaseManager().query("SELECT count(*) as total FROM `uuids` WHERE `discord_id` IS NOT NULL")
                .first().getInt("total");
        } catch (SQLException e) {
            return 0;
        }
    }
}
