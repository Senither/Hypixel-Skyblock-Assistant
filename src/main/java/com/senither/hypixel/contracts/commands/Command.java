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

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.exceptions.FriendlyException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public abstract class Command {

    private static final Logger log = LoggerFactory.getLogger(Command.class);

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

    protected final boolean isGuildMasterOfServerGuild(MessageReceivedEvent event, GuildController.GuildEntry guildEntry) {
        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);
        if (guildReply == null || guildReply.getGuild() == null) {
            throw new FriendlyException("The request to the API returned null for a guild with the given name, try again later.");
        }

        UUID userUUID;
        try {
            Collection result = app.getDatabaseManager().query("SELECT `uuid` FROM `uuids` WHERE `discord_id` = ?", event.getAuthor().getIdLong());
            if (result.isEmpty()) {
                return false;
            }

            userUUID = UUID.fromString(result.first().getString("uuid"));
        } catch (SQLException e) {
            log.error("Failed to get the UUID for {} from the database, error: {}",
                event.getAuthor().getAsTag(), e.getMessage(), e
            );
            return false;
        }

        for (GuildReply.Guild.Member member : guildReply.getGuild().getMembers()) {
            if (member.getRank().equals("Guild Master")) {
                return member.getUuid().equals(userUUID);
            }
        }

        return false;
    }
}
