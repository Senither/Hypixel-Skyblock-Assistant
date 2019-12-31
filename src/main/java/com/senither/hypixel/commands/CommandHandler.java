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

package com.senither.hypixel.commands;

import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.exceptions.CommandAlreadyRegisteredException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    private static final Set<Command> commands = new HashSet<>();
    private static final Pattern argumentsRegEX = Pattern.compile("([^\"]\\S*|\".+?\")\\s*", Pattern.MULTILINE);

    @Nullable
    public static Command getCommand(@Nonnull String message) {
        if (!message.startsWith(Constants.COMMAND_PREFIX)) {
            return null;
        }

        String first = message.split(" ")[0];
        for (Command command : commands) {
            for (String trigger : command.getTriggers()) {
                if (first.equalsIgnoreCase(Constants.COMMAND_PREFIX + trigger)) {
                    return command;
                }
            }
        }
        return null;
    }

    public static void registerCommand(@Nonnull Command command) {
        for (Command registeredCommand : commands) {
            for (String registeredCommandTrigger : registeredCommand.getTriggers()) {
                for (String trigger : command.getTriggers()) {
                    if (registeredCommandTrigger.equalsIgnoreCase(trigger)) {
                        throw new CommandAlreadyRegisteredException(command, trigger);
                    }
                }
            }
        }
        commands.add(command);
    }

    public static void invokeCommand(@Nonnull MessageReceivedEvent event, @Nonnull Command command, boolean invokedThroughMentions) {
        try {
            String[] arguments = toArguments(event.getMessage().getContentRaw());
            command.onCommand(event, Arrays.copyOfRange(arguments, invokedThroughMentions ? 2 : 1, arguments.length));
        } catch (Exception e) {
            log.error("The {} command threw an {} exception, error: {}",
                command.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage(), e
            );
        }
    }

    public static Set<Command> getCommands() {
        return commands;
    }

    private static String[] toArguments(String string) {
        List<String> arguments = new ArrayList<>();

        Matcher matcher = argumentsRegEX.matcher(string.replaceAll("\"\"", "\" \""));
        while (matcher.find()) {
            arguments.add(matcher.group(0)
                .replaceAll("\"", "")
                .trim());
        }

        return arguments.toArray(new String[0]);
    }
}
