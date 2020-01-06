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

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.commands.CommandContainer;
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays a list of all the commands in the bot.",
            "`:command <command>` - Displays info for the given command."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command verify`");
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

        CommandContainer container = app.getCommandManager().getCommand(
            args[0].startsWith(Constants.COMMAND_PREFIX) ? args[0] : Constants.COMMAND_PREFIX + args[0]
        );

        if (container == null) {
            MessageFactory.makeError(event.getMessage(), "Couldn't find a command that uses the `:name` command trigger.")
                .setTitle("Couldn't find command")
                .set("name", args[0])
                .queue();
            return;
        }

        MessageFactory.makeInfo(event.getMessage(), String.join(" ", container.getDescription()))
            .setTitle(container.getName())
            .addField("Usage", formatCommandUsage(container, container.getCommand().getUsageInstructions()), false)
            .addField("Example", formatCommandUsage(container, container.getCommand().getExampleUsage()), false)
            .queue();
    }

    private String formatCommandUsage(CommandContainer container, List<String> message) {
        return String.join("\n", message)
            .replaceAll(":command", Constants.COMMAND_PREFIX + container.getTriggers().get(0));
    }

    private void sendCommandList(MessageReceivedEvent event) {
        PlaceholderMessage message = MessageFactory.makeEmbeddedMessage(event.getChannel())
            .setDescription("For more information about a command, use `:prefixhelp <command>`\nFor example `:prefixhelp verify`")
            .setTitle("Command List")
            .set("prefix", Constants.COMMAND_PREFIX);

        List<CommandContainer> containers = app.getCommandManager().getCommands().stream()
            .sorted(Comparator.comparing(CommandContainer::getCategory))
            .collect(Collectors.toList());

        Map<String, List<Command>> categoryCommands = new LinkedHashMap<>();

        for (CommandContainer container : containers) {
            final String category = container.getCategoryIcon() != null
                ? container.getCategoryIcon().getIcon() + " " + container.getCategory()
                : container.getCategory();

            if (!categoryCommands.containsKey(category)) {
                categoryCommands.put(category, new ArrayList<>());
            }
            categoryCommands.get(category).add(container.getCommand());
        }

        for (Map.Entry<String, List<Command>> commandEntry : categoryCommands.entrySet()) {
            message.addField(
                commandEntry.getKey(),
                "`" + String.join("`, `",
                    commandEntry.getValue().stream()
                        .map(command -> command.getTriggers().get(0))
                        .collect(Collectors.toList())
                ) + "`",
                false
            );
        }

        message.queue();
    }
}
