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

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.time.Carbon;
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
            "Allows the guild leader to sync their guild with the Discord server, when the guild is",
            "synchronized with the Discord server users who verify themselves will automatically",
            "be giving their in-game guild rank, and the bot will periodically re-scan the guild",
            "to promote or demote users depending on their in-game rank."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <guild name>` - Setups the guild for the current Discord server.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command Rollback`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("guild-setup");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the guild name for the guild you're the guild leader for!"
                    + "\nSo that the bot is able to link the server and guild together!"
            ).setTitle("Missing guild name").queue();
            return;
        }

        UUID uuid = getUUIDForUser(event.getAuthor());
        if (uuid == null) {
            MessageFactory.makeError(event.getMessage(), "You must be verified through the bot to use this command!")
                .setTitle("Must be verified!")
                .queue();
            return;
        }

        GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
        if (guildEntry != null && "unlink-guild".equalsIgnoreCase(args[0])) {
            handleUnlinkGuild(event, guildEntry, uuid);
            return;
        }

        if (guildEntry != null) {
            MessageFactory.makeError(event.getMessage(), String.join(" ",
                "The **:name** guild is already linked with a Discord server, you can't link",
                "a guild twice, if you're the guild master of the guild you can unlink the server",
                "by using `:prefixguild-setup unlink-guild` in the Discord server the guild is",
                "currently linked to."
            ))
                .setTitle("Guild is already linked!")
                .set("name", guildEntry.getName())
                .set("prefix", Constants.COMMAND_PREFIX)
                .queue();
            return;
        }

        MessageFactory.makeInfo(event.getMessage(), "Loading guild information...")
            .setTitle("Loading " + String.join(" ", args))
            .queue(message -> app.getHypixel().getGuildByName(String.join("+", args)).whenComplete((guildReply, throwable) -> {
                try {
                    handleGuildRegistration(message, guildReply, throwable, uuid, args);
                } catch (Exception e) {
                    log.error("An error occurred while trying to register a guild, error: {}",
                        e.getMessage(), e
                    );
                }
            }));
    }

    private void handleUnlinkGuild(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, UUID uuid) {
        GuildReply guild = app.getHypixel().getGson().fromJson(
            guildEntry.getData(), GuildReply.class
        );

        GuildReply.Guild.Member fromUUID = getMemberFromUUID(guild.getGuild(), uuid);
        if (fromUUID != null && "Guild Master".equalsIgnoreCase(fromUUID.getRank())) {
            GuildController.deleteGuildWithId(app.getDatabaseManager(), event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(),
                "The **:guildname** have been unlinked with the bot, and will no longer"
                    + " be automatically updated or scanned for rank synchronization."
            ).setTitle("Guild has been unlinked!").queue();
            return;
        }

        MessageFactory.makeError(event.getMessage(),
            "You're not the guild master of the **:guildname** guild, or the guild is not linked to this Discord server!"
        ).setTitle("Unable to unlink").queue();
    }

    private void handleGuildRegistration(Message message, GuildReply guildReply, Throwable throwable, UUID uuid, String[] args) {
        if (throwable != null || guildReply.getGuild() == null) {
            message.editMessage(MessageFactory.makeError(message, "Failed to find any guild on Hypixel called `:error`")
                .set("error", String.join(" ", args))
                .setTitle("Couldn't find guild!")
                .buildEmbed()
            ).queue();
            return;
        }

        GuildReply.Guild.Member currentMember = getMemberFromUUID(guildReply.getGuild(), uuid);
        if (currentMember == null) {
            message.editMessage(MessageFactory.makeError(message, String.join("\n",
                "You're not a member of the **:guildname** guild!",
                "You can't link guilds to servers you're not a member of."
            )).setTitle("Failed to link guild!").buildEmbed()).queue();
            return;
        }

        if (!"Guild Master".equalsIgnoreCase(currentMember.getRank())) {
            message.editMessage(MessageFactory.makeError(message, String.join("\n",
                "You're not the Guild Master of the **:guildname** guild!",
                "You can't link guilds to servers you're not the Guild Master of."
            )).setTitle("Failed to link guild!").buildEmbed()).queue();
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

            message.editMessage(MessageFactory.makeSuccess(message, String.join(" ",
                "The **:guildname** guild have now been linked to the server, users who are already verified",
                "with the bot will soon get their guild rank on the server, and new users who are",
                "verifying themselves with the bot will get their rank when their verification",
                "process is completed successfully!"
                )).setTitle("Guild has been linked!").buildEmbed()
            ).queue();
        } catch (SQLException e) {
            log.error("Failed to create guilds entry for {}, error: {}",
                message.getGuild().getId(), e.getMessage(), e
            );

            message.editMessage(
                MessageFactory.makeError(message, "Something went wrong while trying to create the guild link, error: " + e.getMessage())
                    .setTitle("Failed to link guild!")
                    .buildEmbed()
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
