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
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class AutoRenameCommand extends Command {

    public AutoRenameCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Auto Rename";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command allows the guild master of the guild the server is linked to,",
            "to toggle the auto rename feature on or off, when the feature is enabled,",
            "users with a different Discord name than their in-game Minecraft",
            "username will automatically be renamed to their Minecraft name."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Shows the current auto rename status.",
            "`:command <status>` - Can be used to enable/disable the feature."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command on` - Enables auto renaming.",
            "`:command off` - Disables auto renaming."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("auto-rename", "rename");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
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
                "You must be the guild master of the :name guild to use this command!"
            ).set("name", guildEntry.getName()).setTitle("Missing argument").queue();
            return;
        }

        if (args.length == 0) {
            sendCurrentAutoRenameStatus(event, guildEntry);
            return;
        }

        boolean state = args[0].equalsIgnoreCase("on")
            || args[0].equalsIgnoreCase("enable")
            || args[0].equalsIgnoreCase("true");

        if (state != guildEntry.isAutoRename()) {
            try {
                app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `auto_rename` = ? WHERE `discord_id` = ?",
                    state ? 1 : 0, event.getGuild().getIdLong()
                );

                GuildController.forgetCacheFor(event.getGuild().getIdLong());
            } catch (SQLException e) {
                //
            }
        }

        MessageFactory.makeSuccess(event.getMessage(), String.join("\n", Arrays.asList(
            "The auto rename feature have now been **:status**!",
            "",
            "Users will :type automatically be renamed to their in-game Minecraft username during guild scans."
        )))
            .set("status", state ? "enabled" : "disabled")
            .set("type", state ? "now" : "no longer")
            .queue();
    }

    private void sendCurrentAutoRenameStatus(MessageReceivedEvent event, GuildController.GuildEntry guildEntry) {
        MessageFactory.makeInfo(event.getMessage(), String.join("\n", Arrays.asList(
            "The auto rename feature is currently **:status**!",
            "",
            "You can toggle the feature on and off by using `:command <status>`"
        )))
            .set("command", Constants.COMMAND_PREFIX + getTriggers().get(0))
            .set("status", guildEntry.isAutoRename() ? "enabled" : "disabled")
            .queue();
    }
}
