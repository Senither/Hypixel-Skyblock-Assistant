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

package com.senither.hypixel.commands.administration;

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.commands.administration.settings.AutoRenameCommand;
import com.senither.hypixel.commands.administration.settings.DefaultRoleCommand;
import com.senither.hypixel.commands.administration.settings.GuildMemberRoleCommand;
import com.senither.hypixel.commands.administration.settings.DonationTrackerCommand;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.commands.SettingsSubCommand;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SettingsCommand extends Command {

    private final HashSet<SettingsSubCommand> commands = new HashSet<>();

    public SettingsCommand(SkyblockAssistant app) {
        super(app);

        commands.add(new AutoRenameCommand(app));
        commands.add(new DefaultRoleCommand(app));
        commands.add(new GuildMemberRoleCommand(app));
        commands.add(new DonationTrackerCommand(app));
    }

    @Override
    public String getName() {
        return "Settings Command";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to setup different features in the bot for the server,",
            "or change the behavior of the bot in some ways, allowing Discord servers to",
            "better customize the bot to their needs."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list` - Lists all the different settings types that can be customized.",
            "`:command help <setting>` - Displays helpful information about the setting.",
            "`:command <setting> [option]` - Changes the settings of the given setting type."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command auto-rename on` - Enables auto renaming",
            "`:command auto-role @Member` - Sets the auto role to the Member role."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("settings", "setting", "config");
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
                "You must be the guild master of the **:name** guild to use this command!"
            ).set("name", guildEntry.getName()).setTitle("Missing argument").queue();
            return;
        }

        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must specify the settings action you want to preform to use this command!"
            ).setTitle("Missing argument").queue();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listSettingsAction(event);
                return;

            case "help":
                if (args.length == 1) {
                    MessageFactory.makeError(event.getMessage(),
                        "You must specify the settings action you want to view the help menu for!"
                    ).setTitle("Missing argument").queue();
                    return;
                }

                SettingsSubCommand settingsSubCommand = getSettingsActionFromName(args[1]);
                if (settingsSubCommand == null) {
                    MessageFactory.makeError(event.getMessage(),
                        "The given settings name does not exists, please provide a settings type that exists to see the help menu for it."
                    ).setTitle("Invalid settings").queue();
                    return;
                }

                displayHelpForAction(event, settingsSubCommand);
                return;

            default:
                SettingsSubCommand settingsSubAction = getSettingsActionFromName(args[0]);
                if (settingsSubAction == null) {
                    MessageFactory.makeError(event.getMessage(),
                        "The given settings name does not exists, please provide a settings type that exists to setup or customize the setting."
                    ).setTitle("Invalid settings").queue();
                    return;
                }
                settingsSubAction.onCommand(event, guildEntry, Arrays.copyOfRange(args, 1, args.length));
        }
    }

    private void listSettingsAction(MessageReceivedEvent event) {
        PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(), String.format(String.join("\n",
            "Below you'll find a list of different actions that can be used to customize the bot in various of different ways.",
            "You can get more information about the different settings types by using:\n`%sconfig help <type>`"
        ), Constants.COMMAND_PREFIX)).setTitle("Settings Action List");

        for (SettingsSubCommand command : commands) {
            message.addField(command.getName(), String.format("`%s`", String.join("`, `",
                command.getTriggers()
            )), false);
        }

        message.queue();
    }

    private void displayHelpForAction(MessageReceivedEvent event, SettingsSubCommand command) {
        PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(), String.join(" ", command.getDescription()))
            .setTitle(command.getName())
            .addField("Usage", formatCommandUsage(command, command.getUsageInstructions()), false)
            .addField("Example", formatCommandUsage(command, command.getExampleUsage()), false)
            .set("command", Constants.COMMAND_PREFIX);

        if (command.getTriggers().size() > 1) {
            StringBuilder aliases = new StringBuilder();
            for (int i = 1; i < command.getTriggers().size(); i++) {
                aliases.append(String.format("`%s`, ",
                    command.getTriggers().get(i)
                ));
            }
            message.addField("Aliases", aliases.substring(0, aliases.length() - 2), false);
        }

        message.queue();
    }

    private String formatCommandUsage(SettingsSubCommand command, List<String> message) {
        return String.join("\n", message)
            .replaceAll(":command", Constants.COMMAND_PREFIX + "config " + command.getTriggers().get(0));
    }

    private SettingsSubCommand getSettingsActionFromName(String name) {
        for (SettingsSubCommand command : commands) {
            if (command.getTriggers().contains(name.toLowerCase())) {
                return command;
            }
        }
        return null;
    }
}
