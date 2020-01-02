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

package com.senither.hypixel.commands.general;

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.PlayerReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VerifyCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(VerifyCommand.class);

    public VerifyCommand(SkyblockAssistant app) {
        super(app, false);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("verify");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setColor(MessageType.ERROR.getColor())
                .setTitle("Missing username")
                .setDescription(String.join("\n", Arrays.asList(
                    "You must include your in-game Minecraft username that is linked with your Discord account on Hypixel!",
                    "",
                    "If your account isn't linked you can set it up by logging into `mc.hypixel.net`, then going to your profile, followed by the social button, and then setting your Discord account up there.",
                    "Please not it might take up to a minute before the bot is able to see the changes.",
                    "",
                    "Try again using `h!verify <username>`"
                )))
                .build()
            ).queue();
            return;
        }

        if (!app.getHypixel().isValidMinecraftUsername(args[0])) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setDescription("Invalid Minecraft username given! You must give a valid username to be verified.")
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle("Verifying Account")
            .setDescription("Loading Hypixel player data for " + args[0] + "!")
            .setColor(MessageType.INFO.getColor());


        event.getChannel().sendMessage(embedBuilder.build()).queue(message -> app.getHypixel().getPlayerByName(args[0])
            .whenCompleteAsync(((playerResponse, throwable) -> {
                if (throwable == null) {
                    handleResponse(event, message, playerResponse, embedBuilder);
                    return;
                }

                message.editMessage(embedBuilder
                    .setDescription("Something went wrong: " + throwable.getMessage())
                    .setColor(MessageType.ERROR.getColor())
                    .build()
                ).queue();
            }))
        );
    }

    private void handleResponse(MessageReceivedEvent event, Message message, PlayerReply playerResponse, EmbedBuilder embedBuilder) {
        if (playerResponse.getPlayer() == null) {
            message.editMessage(embedBuilder
                .setDescription("Found no Hypixel profile with the given username!")
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        JsonObject player = playerResponse.getPlayer();
        if (!hasDiscordLinked(player)) {
            sendNoSocialLinksMessage(message, embedBuilder, player);
            return;
        }

        if (!player.getAsJsonObject("socialMedia").getAsJsonObject("links").get("DISCORD").getAsString().equalsIgnoreCase(event.getAuthor().getAsTag())) {
            sendNoSocialLinksMessage(message, embedBuilder, player);
            return;
        }

        try {
            UUID uuid = app.getHypixel().getUUIDFromName(player.get("displayname").getAsString());
            if (uuid != null) {
                app.getDatabaseManager().queryUpdate("UPDATE `uuids` SET `discord_id` = ? WHERE `uuid` = ?",
                    event.getAuthor().getIdLong(), uuid.toString()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        app.getCommandManager().clearVerificationCacheFor(event.getAuthor());

        message.editMessage(embedBuilder
            .setDescription("Your account have now been verified!")
            .setColor(MessageType.SUCCESS.getColor())
            .build()
        ).queue();
    }

    private void sendNoSocialLinksMessage(Message message, EmbedBuilder embedBuilder, JsonObject player) {
        message.editMessage(embedBuilder
            .setDescription("Found no Discord social link that matches your Discord user for " + player.get("displayname").getAsString() + "!")
            .setColor(MessageType.ERROR.getColor())
            .build()
        ).queue();
    }

    private boolean hasDiscordLinked(JsonObject json) {
        try {
            return json.getAsJsonObject("socialMedia").getAsJsonObject("links").has("DISCORD");
        } catch (Exception e) {
            return false;
        }
    }
}
