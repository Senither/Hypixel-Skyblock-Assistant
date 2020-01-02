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

package com.senither.hypixel.contracts.commands;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.commands.statistics.SkillsCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class SkillCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(SkillsCommand.class);

    private final String type;

    public SkillCommand(SkyblockAssistant app, String type) {
        super(app);

        this.type = type.trim().toLowerCase();
    }

    @Override
    public final void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setColor(MessageType.ERROR.getColor())
                .setTitle("Missing username")
                .setDescription(String.format(String.join("\n", Arrays.asList(
                    "You must include the username of the user you want to see %s stats for.",
                    "",
                    "Try again using `h!%s <username>`"
                )), type, getTriggers().get(0)))
                .build()
            ).queue();
            return;
        }

        if (!app.getHypixel().isValidMinecraftUsername(args[0])) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setDescription("Invalid Minecraft username given! You must provide a valid to see the users skills.")
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle(args[0] + "'s " + type.substring(0, 1).toUpperCase() + type.substring(1, type.length()) + "s")
            .setDescription("Loading Skyblock profile data for " + args[0] + "!")
            .setColor(MessageType.INFO.getColor());

        event.getChannel().sendMessage(embedBuilder.build()).queue(message -> {
            app.getHypixel().getSelectedSkyBlockProfileFromUsername(args[0]).whenCompleteAsync((playerReply, throwable) -> {
                if (throwable == null) {
                    try {
                        handleSkyblockProfile(message, playerReply, app.getHypixel().getPlayerByName(args[0]).get(10, TimeUnit.SECONDS));
                        return;
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        throwable = e;
                    }
                }

                log.error("Failed to get player data by name, error: {}", throwable.getMessage(), throwable);

                message.editMessage(embedBuilder
                    .setDescription("Something went wrong: " + throwable.getMessage())
                    .setColor(MessageType.ERROR.getColor())
                    .build()
                ).queue();
            });
        });
    }

    protected abstract void handleSkyblockProfile(Message message, SkyBlockProfileReply playerReply, PlayerReply reply);
}
