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

package com.senither.hypixel.listeners;

import com.senither.hypixel.AppInfo;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.commands.CommandContainer;
import com.senither.hypixel.metrics.MetricType;
import com.senither.hypixel.metrics.Metrics;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.regex.Pattern;

public class MessageEventListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MessageEventListener.class);
    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    private final static String COMMAND_OUTPUT = "Executing Command \"%command%\""
        + "\n\t\tUser:\t %author%"
        + "\n\t\tServer:\t %server%"
        + "\n\t\tChannel: %channel%"
        + "\n\t\tMessage: %message%";

    private static final String directMessage = String.join("\n", Arrays.asList(
        "Hi there!",
        "I'm **%s**, a [Hypixel Skyblock Assistant](https://github.com/Senither/Hypixel-Skyblock-Assistant) built for fun by **Senither#0001**!",
        "You can see what commands I have by using the `%s` command.",
        "",
        "I am currently running **Version %s**",
        "",
        "You can find all of my source code on github:",
        "https://github.com/Senither/Hypixel-Skyblock-Assistant"
    ));

    private final SkyblockAssistant app;

    public MessageEventListener(SkyblockAssistant app) {
        this.app = app;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Metrics.increment(MetricType.MESSAGES_RECEIVED);

        if (event.getAuthor().isBot()) {
            return;
        }

        if (!event.getChannelType().isGuild()) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setDescription(String.format(directMessage,
                    event.getJDA().getSelfUser().getName(),
                    Constants.COMMAND_PREFIX + "help",
                    AppInfo.getAppInfo().version
                ))
                .setColor(Color.decode("#E91E63"))
                .build()
            ).queue();
            return;
        }

        if (!event.getTextChannel().canTalk()) {
            return;
        }

        boolean isMentionable = isMentionableAction(event);

        String part = event.getMessage().getContentRaw().split(" ")[isMentionable ? 1 : 0];

        CommandContainer container = app.getCommandManager().getCommand((isMentionable ? Constants.COMMAND_PREFIX : "") + part);

        if (container != null) {
            log.info(COMMAND_OUTPUT
                .replace("%command%", container.getCommand().getClass().getSimpleName())
                .replace("%author%", generateUsername(event.getMessage()))
                .replace("%channel%", generateChannel(event.getMessage()))
                .replace("%server%", generateServer(event.getMessage()))
                .replace("%message%", event.getMessage().getContentRaw())
            );

            app.getCommandManager().invokeCommand(event, container.getCommand(), isMentionable);
        }
    }

    private boolean isMentionableAction(MessageReceivedEvent event) {
        if (!event.getMessage().isMentioned(event.getGuild().getSelfMember())) {
            return false;
        }

        String[] args = event.getMessage().getContentRaw().split(" ");

        return args.length >= 2 && userRegEX.matcher(args[0]).matches();
    }

    private String generateUsername(Message message) {
        return String.format("%s#%s [%s]",
            message.getAuthor().getName(),
            message.getAuthor().getDiscriminator(),
            message.getAuthor().getId()
        );
    }

    private String generateServer(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
            message.getGuild().getName(),
            message.getGuild().getId()
        );
    }

    private CharSequence generateChannel(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
            message.getChannel().getName(),
            message.getChannel().getId()
        );
    }
}
