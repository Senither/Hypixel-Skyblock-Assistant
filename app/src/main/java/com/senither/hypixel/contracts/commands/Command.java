/*
 * Copyright (c) 2019.
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

package com.senither.hypixel.contracts.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.exceptions.FriendlyException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class Command {

    private static final Logger log = LoggerFactory.getLogger(Command.class);

    private static final Cache<Long, String> usernameCache = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    private static final Cache<Long, UUID> uuidCache = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    protected final SkyblockAssistant app;
    private final boolean verificationRequired;

    public Command(SkyblockAssistant app) {
        this(app, true);
    }

    public Command(SkyblockAssistant app, boolean verificationRequired) {
        this.app = app;
        this.verificationRequired = verificationRequired;
    }

    public abstract String getName();

    public abstract List<String> getDescription();

    public abstract List<String> getUsageInstructions();

    public abstract List<String> getExampleUsage();

    public abstract List<String> getTriggers();

    public abstract void onCommand(MessageReceivedEvent event, String[] args);

    public final boolean isVerificationRequired() {
        return verificationRequired;
    }

    protected final void clearUsernameCacheFor(User user) {
        usernameCache.invalidate(user.getIdLong());
    }

    protected final boolean isGuildMasterOfServerGuild(MessageReceivedEvent event, GuildController.GuildEntry guildEntry) {
        return isPartOfGuildCheck(event, guildEntry, false);
    }

    protected final boolean isGuildMasterOrOfficerOfServerGuild(MessageReceivedEvent event, GuildController.GuildEntry guildEntry) {
        return isPartOfGuildCheck(event, guildEntry, true);
    }

    protected final String getUsernameFromUser(IMentionable user) {
        String username = usernameCache.getIfPresent(user.getIdLong());
        if (username != null) {
            return username;
        }

        try {
            Collection result = app.getDatabaseManager().query(
                "SELECT `username`, `uuid` FROM `uuids` WHERE `discord_id` = ?",
                user.getIdLong()
            );

            if (result.isEmpty()) {
                return null;
            }

            username = result.first().getString("username");
            usernameCache.put(user.getIdLong(), username);
            uuidCache.put(user.getIdLong(), UUID.fromString(result.first().getString("uuid")));

            return username;
        } catch (SQLException e) {
            return null;
        }
    }

    protected final UUID getUUIDFromUser(IMentionable user) {
        UUID uuid = uuidCache.getIfPresent(user.getIdLong());
        if (uuid != null) {
            return uuid;
        }

        try {
            Collection result = app.getDatabaseManager().query(
                "SELECT `username`, `uuid` FROM `uuids` WHERE `discord_id` = ?",
                user.getIdLong()
            );

            if (result.isEmpty()) {
                return null;
            }

            uuid = UUID.fromString(result.first().getString("uuid"));
            uuidCache.put(user.getIdLong(), uuid);
            usernameCache.put(user.getIdLong(), result.first().getString("username"));

            return uuid;
        } catch (SQLException e) {
            return null;
        }
    }

    protected final void sendExceptionMessage(Message message, EmbedBuilder builder, Throwable throwable) {
        if (!(throwable instanceof FriendlyException)) {
            log.error("Failed to get player data by name, error: {}", throwable.getMessage(), throwable);
        }

        String exceptionMessage = (throwable instanceof FriendlyException)
            ? throwable.getMessage()
            : "Something went wrong: " + throwable.getMessage();

        message.editMessage(builder
            .setDescription(exceptionMessage)
            .setColor(MessageType.ERROR.getColor())
            .build()
        ).queue();
    }

    private boolean isPartOfGuildCheck(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, boolean allowOfficers) {
        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);
        if (guildReply == null || guildReply.getGuild() == null) {
            throw new FriendlyException("The request to the API returned null for a guild with the given name, try again later.");
        }

        GuildReply.Guild.Rank rank = guildReply.getGuild().getRanks()
            .stream()
            .sorted((o1, o2) -> o2.getPriority() - o1.getPriority())
            .findFirst().orElse(null);

        if (allowOfficers && rank == null) {
            return false;
        }

        try {
            Collection result = app.getDatabaseManager().query("SELECT `uuid` FROM `uuids` WHERE `discord_id` = ?", event.getAuthor().getIdLong());
            if (result.isEmpty()) {
                return false;
            }

            UUID userUUID = UUID.fromString(result.first().getString("uuid"));
            for (GuildReply.Guild.Member member : guildReply.getGuild().getMembers()) {
                if (!member.getUuid().equals(userUUID)) {
                    continue;
                }

                return member.getRank().equals("Guild Master")
                    || (allowOfficers && member.getRank().equalsIgnoreCase(rank.getName()));
            }
        } catch (SQLException e) {
            log.error("Failed to get the UUID for {} from the database, error: {}",
                event.getAuthor().getAsTag(), e.getMessage(), e
            );
        }
        return false;
    }
}
