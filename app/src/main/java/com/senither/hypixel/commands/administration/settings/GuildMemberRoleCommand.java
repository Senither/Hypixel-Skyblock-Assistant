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

package com.senither.hypixel.commands.administration.settings;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.SettingsSubCommand;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuildMemberRoleCommand extends SettingsSubCommand {

    private static final Logger log = LoggerFactory.getLogger(DefaultRoleCommand.class);

    public GuildMemberRoleCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Guild Member Role";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to setup the guild member role that should automatically be",
            "given to users when they are verified with the bot **and** are in the guild",
            "the server is linked with.",
            "\n",
            "\n> **Note**: When this option is enabled, the guild ranks assignment will be disabled,",
            "meaning the bot won't assign roles matching the users in-game rank in the guild, but",
            "will instead only assign the guild member role that have been setup."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <role name>` - Sets the guild member role that should be used.",
            "`:command disable` - Disables the guild member role feature."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command guild member`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("guildrole", "guild-role", "memberrole", "member-role");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the name of the role you wish to use as "
                    + "the guild member role for the server, or `disable` to disable the "
                    + "default role feature."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            handleDisablingFeature(event);
            return;
        }

        List<Role> roles = event.getGuild().getRolesByName(String.join(" ", args), true);
        if (roles.isEmpty()) {
            MessageFactory.makeError(event.getMessage(), "Failed to find any roles called `:name`, are you sure the role exists?")
                .set("name", String.join(" ", args))
                .queue();
            return;
        }

        Role selectedRole = roles.get(0);

        try {
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `guild_member_role` = ? WHERE `discord_id` = ?",
                selectedRole.getIdLong(), event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(),
                "The :role role will now be used as the guild member role!"
                    + "\nUsers who verify themselves with the bot and are in the guild "
                    + "will now be given the role, and users who are already verified "
                    + "with the bot will automatically be given the role when they "
                    + "join the server if they're in the guild."
            ).set("role", selectedRole.getAsMention()).queue();
        } catch (SQLException e) {
            log.error("Failed to store the selected role ({}) in the database for {}, error: {}",
                selectedRole.getIdLong(), event.getGuild().getIdLong(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Failed to store the default role in the database, error: :error")
                .set("error", e.getMessage())
                .queue();
        }
    }

    private void handleDisablingFeature(MessageReceivedEvent event) {
        try {
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `guild_member_role` = NULL WHERE `discord_id` = ?", event.getGuild().getIdLong());

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "The guild member role have now been disabled!").queue();
        } catch (SQLException e) {
            log.error("Failed to set the default role for {} to null during reset, error: {}",
                event.getGuild().getId(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Something went wrong while trying to reset the servers guild member role!").queue();
        }
    }
}
