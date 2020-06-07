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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.commands.statistics.SkillsCommand;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public abstract class SkillCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(SkillsCommand.class);
    private final ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(3, new ThreadFactoryBuilder()
        .setPriority(Thread.MAX_PRIORITY)
        .setNameFormat("skill-command-%d")
        .build()
    );

    private final String type;

    public SkillCommand(SkyblockAssistant app, String type) {
        super(app);

        this.type = type.trim().toLowerCase();
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command Senither`",
            "`:command @Senither`",
            "`:command Senither Lime`"
        );
    }

    @Override
    public final void onCommand(MessageReceivedEvent event, String[] args) {
        final String username = getUsernameFromMessage(event, args);
        if (username == null) {
            return;
        }

        if (!prepareCommand(event, username)) {
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle(username + "'s " + type.substring(0, 1).toUpperCase() + type.substring(1, type.length()) + "s")
            .setDescription("Loading Skyblock profile data for " + username + "!")
            .setColor(MessageType.INFO.getColor());

        event.getChannel().sendMessage(embedBuilder.build()).queue(message -> {
            if (args.length < 2 || NumberUtil.isNumeric(args[1])) {
                app.getHypixel().getSelectedSkyBlockProfileFromUsername(username).whenCompleteAsync((profileReply, throwable) -> {
                    handleProfileResponse(profileReply, throwable, message, embedBuilder, username,
                        args.length == 0 || args.length == 1 && NumberUtil.isNumeric(args[0])
                            ? args : Arrays.copyOfRange(args, 1, args.length)
                    );
                });
                return;
            }

            String profileName = args[1];

            try {
                PlayerReply playerReply = app.getHypixel().getPlayerByName(username).get(5, TimeUnit.SECONDS);
                if (playerReply == null) {
                    message.editMessage(embedBuilder
                        .setColor(MessageType.ERROR.getColor())
                        .setDescription(String.format("Failed to load player data for **%s**, found no valid player data.",
                            username
                        ))
                        .build()
                    ).queue();
                    return;
                }

                List<String> profileNames = new ArrayList<>();
                JsonObject profiles = playerReply.getPlayer().getAsJsonObject("stats").getAsJsonObject("SkyBlock").getAsJsonObject("profiles");
                for (Map.Entry<String, JsonElement> profileEntry : profiles.entrySet()) {
                    String cuteProfileName = profileEntry.getValue().getAsJsonObject().get("cute_name").getAsString();
                    if (!cuteProfileName.equalsIgnoreCase(profileName)) {
                        profileNames.add(cuteProfileName);
                        continue;
                    }

                    SkyBlockProfileReply profileReply = app.getHypixel().getSkyBlockProfile(profileEntry.getKey()).get(10, TimeUnit.SECONDS);
                    profileReply.getProfile().add("cute_name", profileEntry.getValue().getAsJsonObject().get("cute_name"));

                    handleProfileResponse(profileReply, null, message, embedBuilder, username, Arrays.copyOfRange(args, 2, args.length));
                    return;
                }

                message.editMessage(MessageFactory.makeWarning(message, "Failed to find any valid profile for **:name** called **:profile**")
                    .setTitle("Failed to find profile for " + username)
                    .set("name", username)
                    .set("profile", profileName)
                    .addField("Valid Profiles", "`" + String.join("`, `", profileNames) + "`", false)
                    .buildEmbed()
                ).queue();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                if (e.getCause() instanceof FriendlyException) {
                    message.editMessage(embedBuilder
                        .setColor(MessageType.ERROR.getColor())
                        .setDescription(String.format("Failed to load player data for **%s**!\n%s",
                            username, e.getCause().getMessage()
                        ))
                        .build()
                    ).queue();
                    return;
                }

                log.error("Failed to fetch player data for {}, error: {}",
                    username, e.getMessage(), e
                );
            }
        });
    }

    private void handleProfileResponse(SkyBlockProfileReply profileReply, Throwable throwable, Message message, EmbedBuilder embedBuilder, String username, String[] args) {
        if (throwable != null) {
            sendExceptionMessage(message, embedBuilder, throwable);
            return;
        }

        schedulerService.submit(() -> {
            try {
                PlayerReply playerReply = app.getHypixel().getPlayerByName(username).get(10, TimeUnit.SECONDS);
                if (playerReply != null && playerReply.getPlayer() != null) {
                    UUID uuid = convertStringifiedUUID(playerReply.getPlayer().get("uuid").getAsString());

                    String cachedUsername = app.getHypixel().getUsernameFromUuid(uuid);
                    String currentUsername = playerReply.getPlayer().get("displayname").getAsString();

                    if (cachedUsername != null && !cachedUsername.equalsIgnoreCase(currentUsername)) {
                        updateUsernameForUuidEntry(uuid, currentUsername);
                    }
                }

                handleSkyblockProfile(message, profileReply, playerReply, args);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                sendExceptionMessage(message, embedBuilder, e);
            } catch (Exception e) {
                log.error("An exception where thrown during the {} command, message: {}",
                    getClass().getSimpleName(), e.getMessage(), e
                );
            }
        });
    }

    protected JsonObject getProfileMemberFromPlayer(SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        return profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerReply.getPlayer().get("uuid").getAsString());
    }

    protected String getUsernameFromPlayer(PlayerReply playerReply) {
        String displayName = playerReply.getPlayer().get("displayname").getAsString();
        if (!displayName.startsWith("_")) {
            return displayName;
        }
        return "\\" + displayName;
    }

    private String getUsernameFromMessage(MessageReceivedEvent event, String[] args) {
        if (args.length == 0 || NumberUtil.isNumeric(args[0]) && NumberUtil.parseInt(args[0], 0) < 10) {
            String username = getUsernameFromUser(event.getAuthor());
            if (username != null) {
                return username;
            }

            MessageFactory.makeError(event.getMessage(), String.join("\n", Arrays.asList(
                "You must include the username of the user you want to see :type stats for.",
                "",
                "Try again using `:command <username>`"
            )))
                .setTitle("Missing username")
                .set("command", Constants.COMMAND_PREFIX + getTriggers().get(0))
                .set("type", type)
                .queue();

            return null;
        }

        String username = args[0];
        if (app.getHypixel().isValidMinecraftUsername(username)) {
            return username;
        }

        if (!event.getMessage().getMentions(Message.MentionType.USER).isEmpty()) {
            IMentionable mentionableUser = event.getMessage().getMentions(Message.MentionType.USER).get(
                event.getMessage().isMentioned(event.getGuild().getSelfMember().getUser(), Message.MentionType.USER) ? 1 : 0
            );

            username = getUsernameFromUser(mentionableUser);
            if (username != null) {
                return username;
            }
        }

        event.getChannel().sendMessage(new EmbedBuilder()
            .setDescription("Invalid Minecraft username given!\nYou must provide a valid username to see the users skills.")
            .setColor(MessageType.ERROR.getColor())
            .build()
        ).queue();

        return null;
    }

    private void updateUsernameForUuidEntry(UUID uuid, String newUsername) {
        try {
            app.getDatabaseManager().queryUpdate("UPDATE `uuids` SET `username` = ? WHERE `uuid` = ?",
                newUsername, uuid
            );

            app.getHypixel().forgetUsernameCacheEntry(uuid);
        } catch (SQLException e) {
            log.error("Failed to update username for {}, can't set new username to {}, error: {}",
                uuid, newUsername, e.getMessage(), e
            );
        }
    }

    private UUID convertStringifiedUUID(String uuid) {
        return UUID.fromString("" +
            uuid.substring(0, 8) + "-" +
            uuid.substring(8, 12) + "-" +
            uuid.substring(12, 16) + "-" +
            uuid.substring(16, 20) + "-" +
            uuid.substring(20, 32)
        );
    }

    protected boolean prepareCommand(MessageReceivedEvent event, String username) {
        return true;
    }

    protected abstract void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, String[] args);
}
