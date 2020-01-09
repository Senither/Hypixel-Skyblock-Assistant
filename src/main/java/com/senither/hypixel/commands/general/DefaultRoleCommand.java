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
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DefaultRoleCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(DefaultRoleCommand.class);

    public DefaultRoleCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Default Role";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to setup a default role that should automatically be",
            "given to users when they first join the server, users can then \"upgrade\"",
            "to a guild role by verifying themselves through the bot if they're also in",
            "the guild that is setup for the server.",
            "\n",
            "\n> **Note**: When this option is enabled, all users with a guild role who",
            "aren't verified will have their guild role replaced with the default role",
            "instead, until they verify themselves through the bot to verify they are in the guild."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <role name>` - Sets the default role that should be used.",
            "`:command disable` - Disables the default role feature."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList("`:command guest`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("default-role", "drole");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the name of the role you wish to use as"
                    + "the default role for the server, or `disable` to disable the "
                    + "default role feature."
            ).setTitle("Missing argument").queue();
            return;
        }

        GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
        if (guildEntry == null) {
            MessageFactory.makeError(event.getMessage(),
                "The server is not currently setup with a guild, you must setup "
                    + "the server with a guild before you can use this command!"
            ).setTitle("Server is not setup").queue();
            return;
        }

        if (!isGuildMasterOfServerGuild(event, guildEntry)) {
            MessageFactory.makeError(event.getMessage(),
                "You must be the guild master to use this command!"
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
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `default_role` = ? WHERE `discord_id` = ?",
                selectedRole.getIdLong(), event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(),
                "The :role role will now be used as the default role!"
                    + "\nNew users who join the server will automatically be given the "
                    + "role until they verify themselves with the bot, and users with "
                    + "existing guild roles will have their roles replaced with the "
                    + "default until they verify themselves with the bot."
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
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `default_role` = NULL WHERE `discord_id` = ?", event.getGuild().getIdLong());

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "The default role have now been disabled!").queue();
        } catch (SQLException e) {
            log.error("Failed to set the default role for {} to null during reset, error: {}",
                event.getGuild().getId(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Something went wrong while trying to reset the servers default role!").queue();
        }
    }
}
