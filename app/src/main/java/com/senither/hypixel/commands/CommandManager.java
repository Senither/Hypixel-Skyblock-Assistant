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
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.commands.middlewares.ThrottleMiddleware;
import com.senither.hypixel.commands.middlewares.VerificationMiddleware;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.commands.Middleware;
import com.senither.hypixel.exceptions.CommandAlreadyRegisteredException;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.metrics.Metrics;
import io.prometheus.client.Histogram;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);
    private static final Set<CommandContainer> commands = new HashSet<>();
    private static final Pattern argumentsRegEX = Pattern.compile("([^\"]\\S*|\".+?\")\\s*", Pattern.MULTILINE);
    private static final List<Middleware> middlewares = Arrays.asList(
        new VerificationMiddleware(),
        new ThrottleMiddleware()
    );

    private final SkyblockAssistant app;

    public CommandManager(SkyblockAssistant app) {
        this.app = app;
    }

    @Nullable
    public CommandContainer getCommand(@Nonnull String message) {
        if (!message.toLowerCase().startsWith(Constants.COMMAND_PREFIX)) {
            return null;
        }

        String first = message.split(" ")[0];
        for (CommandContainer container : commands) {
            for (String trigger : container.getTriggers()) {
                if (first.equalsIgnoreCase(Constants.COMMAND_PREFIX + trigger)) {
                    return container;
                }
            }
        }
        return null;
    }

    public void registerCommand(@Nonnull Command command) {
        for (CommandContainer registeredCommand : commands) {
            for (String registeredCommandTrigger : registeredCommand.getTriggers()) {
                for (String trigger : command.getTriggers()) {
                    if (registeredCommandTrigger.equalsIgnoreCase(trigger)) {
                        throw new CommandAlreadyRegisteredException(command, trigger);
                    }
                }
            }
        }
        commands.add(new CommandContainer(command));
    }

    public void invokeCommand(@Nonnull MessageReceivedEvent event, @Nonnull Command command, boolean invokedThroughMentions) {
        Metrics.commandsReceived.labels(command.getClass().getSimpleName()).inc();
        Histogram.Timer timer = Metrics.executionTime.labels(command.getClass().getSimpleName()).startTimer();

        try {
            String[] arguments = toArguments(event.getMessage().getContentRaw());

            for (Middleware middleware : middlewares) {
                if (!middleware.handle(app, event, command)) {
                    return;
                }
            }

            command.onCommand(event, Arrays.copyOfRange(arguments, invokedThroughMentions ? 2 : 1, arguments.length));

            Metrics.commandsExecuted.labels(command.getClass().getSimpleName()).inc();
            Metrics.commandsExecutedByGuild.labels(event.getGuild().getName()).inc();
        } catch (FriendlyException e) {
            MessageFactory.makeError(event.getMessage(), e.getMessage()).queue();
        } catch (Exception e) {
            Metrics.commandExceptions.labels(e.getClass().getSimpleName()).inc();

            log.error("The {} command threw an {} exception, error: {}",
                command.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage(), e
            );
        } finally {
            if (timer != null) {
                timer.observeDuration();
            }
        }
    }

    public Set<CommandContainer> getCommands() {
        return commands;
    }

    private String[] toArguments(String string) {
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
