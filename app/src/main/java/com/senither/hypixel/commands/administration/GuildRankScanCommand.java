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
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.reports.ReportService;
import com.senither.hypixel.time.Carbon;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GuildRankScanCommand extends Command {

    public GuildRankScanCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Guild Rank Scan";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command will queue up a complete guild scan for every member in the guild, and compare",
            "their stats with the rank requirements setup through the `:commandrank-requirement`, and",
            "then generate a report that is viewable through a website."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command` - Starts a complete guild scan."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("guild-scan", "guildscan");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (!app.getConfiguration().getServlet().isEnabled()) {
            MessageFactory.makeError(event.getMessage(),
                "The command is disabled globally due to a bot configuration setting being disabled.\n"
                    + "The web servlet setting must be enabled for this command to work properly."
            ).setTitle("Command is disabled!").queue();
            return;
        }

        GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
        if (guildEntry == null) {
            MessageFactory.makeError(event.getMessage(),
                "The server is not currently setup with a guild, you must setup "
                    + "the server with a guild before you can use this command!"
            ).setTitle("Server is not setup").queue();
            return;
        }

        if (guildEntry.getRankRequirements().isEmpty()) {
            MessageFactory.makeError(event.getMessage(),
                "There are currently no rank requirements setup for the server, you "
                    + "must first setup the requirements for the different guild ranks you want "
                    + "to be able to check for using `:command`."
            )
                .set("command", Constants.COMMAND_PREFIX + "rank-requirement")
                .setTitle("Rank Requirements is not setup")
                .queue();
            return;
        }

        if (!isGuildMasterOrOfficerOfServerGuild(event, guildEntry)) {
            MessageFactory.makeError(event.getMessage(),
                "You must be the guild master or an officer of the **:name** guild to use this command!"
            ).set("name", guildEntry.getName()).setTitle("Missing permissions").queue();
            return;
        }

        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);
        Carbon time = Carbon.now().addSeconds(guildReply.getGuild().getMembers().size() * 10);

        try {
            UUID uuid = ReportService.createReportFor(app, guildEntry);
            if (uuid == null) {
                MessageFactory.makeWarning(event.getMessage(),
                    "A guild scan report have already been queued, please wait for the first report to finish before starting another one."
                ).setTitle("Existing report is queued").queue();
                return;
            }

            MessageFactory.makeInfo(event.getMessage(), String.join("\n", Arrays.asList(
                "The guild scan have been queued!",
                "The scan will take as estimate of :time to be completed.",
                "You can view the completed scan by [clicking here](:link), or using the ID below.",
                "",
                "[:link](:link)"
            )))
                .setTitle("Guild Scan Queued")
                .set("id", uuid)
                .set("link", app.getConfiguration().getServlet().generateReportUrl(uuid))
                .set("time", time.diffForHumans(true))
                .queue();
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(), "Failed to start a new guild scan report due to a database error\n\n:message")
                .set("message", e.getMessage())
                .queue();
        }
    }
}
