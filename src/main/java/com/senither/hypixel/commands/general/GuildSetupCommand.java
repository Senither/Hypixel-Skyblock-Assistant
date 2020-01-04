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

package com.senither.hypixel.commands.general;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.time.Carbon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GuildSetupCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(GuildSetupCommand.class);

    public GuildSetupCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Guild Setup";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Allows the guild leader to sync their guild with the Discord server when the guild is",
            "synchronized with the Discord server, users who verify themselves will automatically",
            "be giving their in-game guild rank, and the bot will periodically re-scan the guild",
            "to promote or demote users depending on their in-game rank."

        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("guild-setup");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Missing guild name")
                .setColor(MessageType.ERROR.getColor())
                .setDescription(
                    "You must include the guild name for the guild you're the guild leader for!"
                        + "\nSo that the bot is able to link the server and guild together!"
                )
                .build()
            ).queue();
            return;
        }

        UUID uuid = getUUIDForUser(event.getAuthor());
        if (uuid == null) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Must be verified!")
                .setColor(MessageType.ERROR.getColor())
                .setDescription("You must be verified through the bot to use this command!")
                .build()
            ).queue();
            return;
        }

        try {
            Collection query = app.getDatabaseManager().query(
                "SELECT `id`, `data` FROM `guilds` WHERE `discord_id` = ?",
                event.getGuild().getIdLong()
            );

            if (!query.isEmpty() && "unlink-guild".equalsIgnoreCase(args[0])) {
                GuildReply guild = app.getHypixel().getGson().fromJson(
                    query.first().getString("data"), GuildReply.class
                );

                GuildReply.Guild.Member fromUUID = getMemberFromUUID(guild.getGuild(), uuid);
                if (fromUUID != null && "Guild Master".equalsIgnoreCase(fromUUID.getRank())) {
                    app.getDatabaseManager().queryUpdate(
                        "DELETE FROM `guilds` WHERE `discord_id` = ?",
                        event.getGuild().getIdLong()
                    );

                    event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle("Guild has been unlinked!")
                        .setDescription(String.format(String.join(" ",
                            "The **%s** have been unlinked with the bot, and will no longer",
                            "be automatically updated or scanned for rank synchronization."
                        ), guild.getGuild().getName()))
                        .setColor(MessageType.SUCCESS.getColor())
                        .build()
                    ).queue();
                    return;
                }

                event.getChannel().sendMessage(new EmbedBuilder()
                    .setTitle("Unable to unlink")
                    .setDescription(String.format(String.join(" ",
                        "You're not the guild master of the **%s** guild, or the guild is not linked to this Discord server!"
                    ), guild.getGuild().getName()))
                    .setColor(MessageType.ERROR.getColor())
                    .build()
                ).queue();
                return;
            }

            if (!query.isEmpty()) {
                event.getChannel().sendMessage(new EmbedBuilder()
                    .setTitle("Guild is already linked!")
                    .setDescription(String.format(String.join(" ",
                        "The **%s** guild is already linked with a Discord server, you can't link",
                        "a guild twice, if you're the guild master of the guild you can unlink the server",
                        "by using `h!guild-setup unlink-guild` in the Discord server the guild is",
                        "currently linked to."
                    ), String.join(" ", args)))
                    .setColor(MessageType.ERROR.getColor())
                    .build()
                ).queue();
                return;
            }
        } catch (SQLException e) {
            log.error("Failed to check if guild is already synced with the bot, error: {}",
                e.getMessage(), e
            );

            event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("An error occurred!")
                .setDescription(String.format(
                    "An error occurred while checking if the guild is already linked with a Discord server, error: %s",
                    e.getMessage()
                ))
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        event.getChannel().sendMessage(new EmbedBuilder()
            .setDescription("Loading guild information...")
            .setTitle("Loading " + String.join(" ", args))
            .setColor(MessageType.INFO.getColor())
            .build()
        ).queue(message -> app.getHypixel().getGuildByName(String.join(" ", args)).whenComplete((guildReply, throwable) -> {
            try {
                handleGuildRegistration(message, guildReply, throwable, uuid, args);
            } catch (Exception e) {
                log.error("An error occurred while trying to register a guild, error: {}",
                    e.getMessage(), e
                );
            }
        }));
    }

    private void handleGuildRegistration(Message message, GuildReply guildReply, Throwable throwable, UUID uuid, String[] args) {
        if (throwable != null || guildReply.getGuild() == null) {
            message.editMessage(new EmbedBuilder()
                .setTitle("Couldn't find guild!")
                .setColor(MessageType.ERROR.getColor())
                .setDescription(String.format(
                    "Failed to find any guild on Hypixel called `%s`",
                    String.join(" ", args)
                ))
                .build()
            ).queue();
            return;
        }

        GuildReply.Guild.Member currentMember = getMemberFromUUID(guildReply.getGuild(), uuid);
        if (currentMember == null) {
            message.editMessage(new EmbedBuilder()
                .setTitle("Failed to link guild!")
                .setDescription(String.format(
                    "You're not a member of the **%s** guild!\nYou can't link guilds to servers you're not a member of.",
                    guildReply.getGuild().getName()
                ))
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        if (!"Guild Master".equalsIgnoreCase(currentMember.getRank())) {
            message.editMessage(new EmbedBuilder()
                .setTitle("Failed to link guild!")
                .setDescription(String.format(
                    "You're not the Guild Master of the **%s** guild!\nYou can't link guilds to servers you're not the Guild Master of.",
                    guildReply.getGuild().getName()
                ))
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        try {
            app.getDatabaseManager().queryInsert(
                "INSERT INTO `guilds` (`id`, `discord_id`, `name`, `data`, `last_updated_at`) VALUES (?, ?, ?, ?, ?)",
                guildReply.getGuild().get_id(),
                message.getGuild().getIdLong(),
                guildReply.getGuild().getName(),
                app.getHypixel().getGson().toJson(guildReply),
                Carbon.now().subMinutes(5).toDateTimeString()
            );

            message.editMessage(new EmbedBuilder()
                .setTitle("Guild has been linked!")
                .setDescription(String.format(String.join(" ",
                    "The **%s** guild have now been linked to the server, users who are already verified",
                    "with the bot will soon get their guild rank on the server, and new users who are",
                    "verifying themselves with the bot will get their rank when their verification",
                    "process is completed successfully!"
                ), guildReply.getGuild().getName()))
                .setColor(MessageType.SUCCESS.getColor())
                .build()
            ).queue();
        } catch (SQLException e) {
            log.error("Failed to create guilds entry for {}, error: {}",
                message.getGuild().getId(), e.getMessage(), e
            );

            message.editMessage(new EmbedBuilder()
                .setTitle("Failed to link guild!")
                .setDescription("Something went wrong while trying to create the guild link, error: " + e.getMessage())
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
        }
    }

    private GuildReply.Guild.Member getMemberFromUUID(GuildReply.Guild guild, UUID uuid) {
        for (GuildReply.Guild.Member member : guild.getMembers()) {
            if (uuid.equals(member.getUuid())) {
                return member;
            }
        }
        return null;
    }

    private UUID getUUIDForUser(User user) {
        try {
            Collection result = app.getDatabaseManager().query(
                "SELECT `uuid` FROM `uuids` WHERE `discord_id` = ?",
                user.getIdLong()
            );

            if (!result.isEmpty()) {
                return UUID.fromString(result.first().getString("uuid"));
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
