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

package com.senither.hypixel.commands.misc;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.commands.SkillCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class HelpCommand extends Command {

    public HelpCommand(SkyblockAssistant app) {
        super(app, false);
    }

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Displays a list of commands that can be use in the bot,",
            "as-well-as some general information about the bot and ",
            "its creator, and where to find the code for the bot."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("help");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            sendCommandList(event);
            return;
        }

        Command command = app.getCommandManager().getCommand("h!" + args[0]);
        if (command == null) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Couldn't find command")
                .setDescription(String.format(
                    "Couldn't find a command that uses the `%s` command trigger.",
                    args[0]
                ))
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        event.getChannel().sendMessage(new EmbedBuilder()
            .setTitle(command.getName())
            .setColor(MessageType.INFO.getColor())
            .setDescription(String.join(" ", command.getDescription()))
            .addField("Usage", createCommandUsage(command), true)
            .build()
        ).queue();
    }

    private String createCommandUsage(Command command) {
        String commandArg = command instanceof SkillCommand ? " <username>" : "";

        return "```h!" + String.join(commandArg + "\nh!", command.getTriggers()) + commandArg + "```";
    }

    private void sendCommandList(MessageReceivedEvent event) {
        event.getChannel().sendMessage(
            String.join("\n", Arrays.asList(
                "> __**General Commands**__```",
                "> h!verify <username>  - Verifies your Discord account with the bot",
                "> h!guild-setup <name> - Links the Discord server together with the given guild name through the bot",
                "> ```",
                "> __**Statistics Commands**__ ```",
                "> h!skills <username> - Returns the Skill levels of a player",
                "> h!slayer <username> - Returns the Slayer levels of a player",
                "> h!ah <username>     - Returns the Auction House stats of a player",
                "> ```",
                "> __**Misc Commands**__ ```",
                "> h!ping              - Can be used to get the ping of the bot to Discord",
                "> h!help [command]    - Returns this list of commands, or info about a specific command",
                "> ```",
                "> __**General Information**__",
                "> This bot was created by Senither, a self-hostable Hypixel Skyblock Assistant for your",
                "> Discord server! You can find all the source code, as-well-as instructions on how to",
                "> run your own version of the bot at:",
                "> https://github.com/Senither/Hypixel-Skyblock-Assistant"
            ))
        ).queue();
    }
}
