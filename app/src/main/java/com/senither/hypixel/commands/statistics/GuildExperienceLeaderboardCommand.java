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

package com.senither.hypixel.commands.statistics;

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.sql.SQLException;
import java.util.*;

public class GuildExperienceLeaderboardCommand extends Command {

    public GuildExperienceLeaderboardCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Guild Experience Leaderboard";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to get the Guild Experience leaderboard for the guild",
            "that is linked to the Discord server the command is ran in, the XP used is",
            "the combined GXP the user has earned for the guild over the last 7 days."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command [page]` - Shows the given page of the leaderboard."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 2`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList(
            "expleaderboard", "gxplead", "glead", "gtop"
        );
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
        if (guildEntry == null || guildEntry.getData() == null) {
            MessageFactory.makeError(event.getMessage(),
                "The server is not currently setup with a guild, you must setup "
                    + "the server with a guild before you can use this command!"
            ).setTitle("Server is not setup").queue();
            return;
        }

        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);
        if (guildReply == null || guildReply.getGuild() == null) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying ot load the guild data from the database, try again later."
            ).queue();
            return;
        }

        MessageFactory.makeEmbeddedMessage(event.getChannel())
            .setTitle("Guild Experience Leaderboard")
            .setDescription("Generating leaderboard data...")
            .queue(message -> handleGuildExperienceLeaderboard(guildReply, message, args));
    }

    private void handleGuildExperienceLeaderboard(GuildReply guildReply, Message message, String[] args) {
        Map<UUID, Long> unsortedUsers = new HashMap<>();
        for (GuildReply.Guild.Member member : guildReply.getGuild().getMembers()) {
            long experience = 0L;

            for (Map.Entry<String, Long> entry : member.getExpHistory().entrySet()) {
                experience += entry.getValue();
            }

            unsortedUsers.put(member.getUuid(), experience);
        }

        Map<UUID, Long> sortedUsers = new LinkedHashMap<>();
        unsortedUsers.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .forEach(user -> sortedUsers.put(user.getKey(), user.getValue()));

        SimplePaginator<Long> paginator = new SimplePaginator<>(sortedUsers, 10);

        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        StringBuilder embedDescription = new StringBuilder();
        paginator.forEach((index, key, val) -> {
            String usernameFromUuid = "-- Unavailable Username --";
            try {
                usernameFromUuid = app.getHypixel().getUsernameFromUuid(UUID.fromString(key.toString()));
                if (usernameFromUuid.startsWith("_")) {
                    usernameFromUuid = "\\" + usernameFromUuid;
                }
            } catch (SQLException ignored) {
                //
            }

            embedDescription
                .append('`')
                .append(index + 1)
                .append('`')
                .append(String.format(" **%s** has earned **%s** GXP!",
                    usernameFromUuid,
                    NumberUtil.formatNicely(val)
                ))
                .append("\n");
        });

        embedDescription
            .append("\n")
            .append(paginator.generateFooter(
                Constants.COMMAND_PREFIX + getTriggers().get(0)
            ));

        message.editMessage(
            MessageFactory.makeInfo(message, embedDescription.toString())
                .setTitle("Guild Experience Leaderboard")
                .setFooter("The GXP is the combined Guild XP the user has earned over the last 7 days.")
                .buildEmbed()
        ).queue();
    }
}
