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
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InformationCommand extends Command {

    public InformationCommand(SkyblockAssistant app) {
        super(app, false);
    }

    @Override
    public String getName() {
        return "Bot Information";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets some general information about the bot, like how it works, and where",
            "to find the code for the project, who created the bot, etc."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Gets info about the bot");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("info", "botinfo");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        String author = "Senither#0001";
        Member memberById = event.getGuild().getMemberById(88739639380172800L);
        if (memberById != null) {
            author = memberById.getAsMention();
        }

        event.getChannel().sendMessage(new EmbedBuilder()
            .setTitle("Bot Information")
            .setDescription(String.format(
                "The bot was created by %s, you can find the source code for the project at: https://github.com/Senither/Hypixel-Skyblock-Assistant",
                author
            ))
            .addField("How does it work?", String.join(" ", Arrays.asList(
                "The bot works by using [Hypixels API](https://api.hypixel.net) to request",
                "information about users when they use commands, which is then stored in a",
                "MySQL database so the data can be used later and the bot doesn't need to",
                "send as many requests to the API."
            )), false)
            .addField("How does it select my profile?", String.join(" ", Arrays.asList(
                "When using a stats command, the bot will load every Skyblock profile you",
                "have, and then use the profile that was last active, however profile data",
                "is stored for up to 1 hour, so if you swap between profiles often the bot",
                "might have your old profile cached."
            )), false)
            .addField("Can I use the bot on my own server?", String.join(" ", Arrays.asList(
                "If the bot is listed as public with Discord, other people than the owner",
                "will also be able to invite the bot to their own servers, however if the",
                "bot is listed as private you can create your own version of the bot by",
                "self-hosting it, you can find a guide on how to do this at",
                "[Senither/Hypixel-Skyblock-Assistant](https://github.com/Senither/Hypixel-Skyblock-Assistant#table-of-content)"
            )), false)
            .build()
        ).queue();
    }
}
