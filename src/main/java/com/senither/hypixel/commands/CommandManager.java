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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.exceptions.CommandAlreadyRegisteredException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);
    private static final Set<CommandContainer> commands = new HashSet<>();
    private static final Pattern argumentsRegEX = Pattern.compile("([^\"]\\S*|\".+?\")\\s*", Pattern.MULTILINE);

    private static final Cache<Long, Boolean> verifyCache = CacheBuilder.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build();

    private final SkyblockAssistant app;

    public CommandManager(SkyblockAssistant app) {
        this.app = app;
    }

    @Nullable
    public CommandContainer getCommand(@Nonnull String message) {
        if (!message.startsWith(Constants.COMMAND_PREFIX)) {
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
        try {
            String[] arguments = toArguments(event.getMessage().getContentRaw());
            if (!command.isVerificationRequired() || isUserVerified(event.getAuthor())) {
                command.onCommand(event, Arrays.copyOfRange(arguments, invokedThroughMentions ? 2 : 1, arguments.length));
                return;
            }

            event.getChannel().sendMessage(new EmbedBuilder()
                .setColor(com.senither.hypixel.chat.MessageType.ERROR.getColor())
                .setTitle("Missing verification")
                .setDescription(String.join("\n", Arrays.asList(
                    "You must verify your account with the bot to use this command, you can do this by",
                    "running `h!verify <username>`, where your username is your in-game Minecraft",
                    "username that has your Discord account linked on Hypixel.net"
                )))
                .build()
            ).queue();
        } catch (Exception e) {
            log.error("The {} command threw an {} exception, error: {}",
                command.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage(), e
            );
        }
    }

    public Set<CommandContainer> getCommands() {
        return commands;
    }

    public void clearVerificationCacheFor(User user) {
        verifyCache.invalidate(user.getIdLong());
    }

    private boolean isUserVerified(User user) {
        Boolean verificationState = verifyCache.getIfPresent(user.getIdLong());
        if (verificationState != null) {
            return verificationState;
        }

        try {
            boolean hasResult = !app.getDatabaseManager().query("SELECT `uuid` FROM `uuids` WHERE `discord_id` = ?", user.getIdLong()).isEmpty();

            verifyCache.put(user.getIdLong(), hasResult);
            return hasResult;
        } catch (SQLException ignored) {
            return false;
        }
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
