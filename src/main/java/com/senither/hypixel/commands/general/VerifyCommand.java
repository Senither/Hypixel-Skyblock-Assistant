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

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.hypixel.responses.PlayerResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VerifyCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(VerifyCommand.class);

    public VerifyCommand(SkyblockAssistant app) {
        super(app);
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

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle("Verifying Account")
            .setDescription("Loading Hypixel player data for " + args[0] + "!")
            .setColor(MessageType.INFO.getColor());

        event.getChannel().sendMessage(embedBuilder.build()).queue(message -> app.getHypixelAPI().getPlayer(args[0])
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

    private void handleResponse(MessageReceivedEvent event, Message message, PlayerResponse playerResponse, EmbedBuilder embedBuilder) {
        if (playerResponse.getPlayer() == null) {
            message.editMessage(embedBuilder
                .setDescription("Found no Hypixel profile with the given username!")
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        PlayerResponse.Player player = playerResponse.getPlayer();

        if (!hasSocialMediaLinked(player)) {
            sendNoSocialLinksMessage(message, embedBuilder, player);
            return;
        }

        if (!player.getSocialMedia().getLinks().getOrDefault("DISCORD", "invalid").equalsIgnoreCase(event.getAuthor().getAsTag())) {
            sendNoSocialLinksMessage(message, embedBuilder, player);
            return;
        }

        // TODO: Create a database entry here to store the users Discord ID and the Minecraft UUID for the user, so we an create a link between them that can be used later for other requests.

        message.editMessage(embedBuilder
            .setDescription("Your account have now been verified!")
            .setColor(MessageType.SUCCESS.getColor())
            .build()
        ).queue();
    }

    private void sendNoSocialLinksMessage(Message message, EmbedBuilder embedBuilder, PlayerResponse.Player player) {
        message.editMessage(embedBuilder
            .setDescription("Found no Discord social link that matches your Discord user for " + player.getDisplayname() + "!")
            .setColor(MessageType.ERROR.getColor())
            .build()
        ).queue();
    }

    private boolean hasSocialMediaLinked(PlayerResponse.Player player) {
        try {
            return !player.getSocialMedia().getLinks().isEmpty();
        } catch (NullPointerException e) {
            return false;
        }
    }
}
