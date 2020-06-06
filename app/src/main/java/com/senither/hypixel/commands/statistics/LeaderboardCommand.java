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
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.hypixel.leaderboard.LeaderboardType;
import com.senither.hypixel.hypixel.response.GuildLeaderboardResponse;
import com.senither.hypixel.hypixel.response.GuildMetricsResponse;
import com.senither.hypixel.hypixel.response.PlayerLeaderboardResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeaderboardCommand extends Command {

    public LeaderboardCommand(SkyblockAssistant app) {
        super(app, false);
    }

    @Override
    public String getName() {
        return "Guild Leaderboard";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to show a list of guilds that are being tracked by the bot,",
            "ordered in descending order by the guild average skill level and slayer XP.\n",
            "The command also supports viewing player leaderboards for players in the track guilds,",
            "allowing you to see skills, slayers, and skill type leaderboards for players in guilds."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Lists all the tracked guilds",
            "`:command <type> <guild name>` - Shows a leaderboard with the given type for the given guild"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command`",
            "`:command skills Some Name`",
            "`:command slayer Some Name`",
            "`:command farming Some Name`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList(
            "leaderboard", "lead", "top"
        );
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0 || NumberUtil.isNumeric(args[0])) {
            showGuildLeaderboard(event, args);
            return;
        }

        LeaderboardType type = LeaderboardType.fromName(args[0].trim());
        if (type == null) {
            List<String> types = new ArrayList<>();
            for (LeaderboardType leaderboardType : LeaderboardType.values()) {
                types.add(leaderboardType.getAliases().get(0));
            }

            MessageFactory.makeError(event.getMessage(),
                "Invalid type given, please provide one of the types below to see a leaderboard for your desired guild.\n`:types`"
            ).set("types", String.join("`, `", types)).setTitle("Invalid leaderboard type").queue();
            return;
        }

        UUID userUUID = null;
        final int[] position = {-1};

        try {
            userUUID = app.getHypixel().getUUIDFromUser(event.getAuthor());
        } catch (SQLException ignored) {
        }

        int pageNumber = 1;
        String guildName;
        GuildLeaderboardResponse.Guild guild;
        if (args.length == 1 || NumberUtil.isNumeric(args[1])) {
            GuildController.GuildEntry guildById = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
            if (guildById == null) {
                MessageFactory.makeError(event.getMessage(),
                    "You must also specify the name of the guild you wish to see the stats for."
                ).setTitle("Missing guild name").queue();
                return;
            }
            guildName = guildById.getName();
            guild = getGuildFromName(guildById.getName());
            if (NumberUtil.isNumeric(args[args.length - 1])) {
                pageNumber = NumberUtil.parseInt(args[args.length - 1], 1);
            }
        } else {
            guildName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (NumberUtil.isNumeric(args[args.length - 1])) {
                pageNumber = NumberUtil.parseInt(args[args.length - 1], 1);
                guildName = guildName.substring(0, guildName.length() - args[args.length - 1].length()).trim();
            }
            guild = getGuildFromName(guildName);
        }

        if (guild == null) {
            MessageFactory.makeError(event.getMessage(),
                "There are no guild called `:name` that are being tracked by the bot currently, "
                    + "please provide the name of a guild that the bot is already tracking to see their player leaderboard."
            ).set("name", guildName).setTitle("Invalid guild name").queue();
            return;
        }

        if (type.equals(LeaderboardType.OVERVIEW)) {
            showGuildOverview(event, guild, args);
            return;
        }

        PlayerLeaderboardResponse leaderboard = app.getHypixel().getGuildPlayersLeaderboard(guild.getId());
        if (leaderboard == null || !leaderboard.isSuccess()) {
            MessageFactory.makeError(event.getMessage(),
                "The `:name` guild does not appear to have any player stats being tracked right now, try again later."
            ).set("name", guildName).setTitle("Missing guild data").queue();
            return;
        }

        final String rowMessage = type.getExpFunction() == null
            ? "%s: %s\n%s> %s " : type.equals(LeaderboardType.AVERAGE_SKILL)
            ? "%s: %s\n%s> %s (%s)" : "%s: %s\n%s> %s [%s XP]";

        final int[] index = {1};
        final UUID finalUserUUID = userUUID;
        List<String> completeRows = new ArrayList<>();
        leaderboard.getData().stream()
            .sorted((o1, o2) -> Double.compare(type.getOrderFunction().getStat(o2), type.getOrderFunction().getStat(o1)))
            .forEach(player -> {
                if (player.getUuid().equals(finalUserUUID)) {
                    position[0] = index[0];
                }
                completeRows.add(String.format(rowMessage,
                    padPosition("#" + NumberUtil.formatNicely(index[0]), index[0] - 1),
                    player.getUsername(),
                    padPosition("", index[0] - 1),
                    type.getStatFunction().getStat(player) == -1
                        ? "API IS DISABLED"
                        : NumberUtil.formatNicelyWithDecimals(type.getStatFunction().getStat(player)),
                    type.getExpFunction() == null ? "" : type.getExpFunction().getStat(player) == -1
                        ? "API IS DISABLED"
                        : NumberUtil.formatNicelyWithDecimals(type.getExpFunction().getStat(player))
                ));
                index[0]++;
            });

        List<String> rows = new ArrayList<>();
        SimplePaginator<String> paginator = new SimplePaginator<>(completeRows, 10, pageNumber);
        paginator.forEach((index1, key, val) -> rows.add(val));

        String command = String.format("%s%s %s %s",
            Constants.COMMAND_PREFIX, getTriggers().get(0),
            type.getAliases().get(0), guild.getName()
        );

        String note = "";
        if (finalUserUUID != null && position[0] > 0) {
            note = String.format("> You're ranked **#%s** in %s in the guild!\n\n",
                position[0], type.getName()
            );
        }

        MessageFactory.makeInfo(event.getMessage(), String.format(
            "```ada\n%s```",
            String.join("\n", rows)) + "\n"
            + note + paginator.generateFooter(command)
        )
            .setTitle(String.format("%s's %s Leaderboard", guild.getName(), type.getName()))
            .setFooter("Requested by " + event.getAuthor().getAsTag(), event.getAuthor().getEffectiveAvatarUrl())
            .setTimestamp(Carbon.now().getTime().toInstant())
            .queue();
    }

    private void showGuildOverview(MessageReceivedEvent event, GuildLeaderboardResponse.Guild guild, String[] args) {
        GuildMetricsResponse metrics = app.getHypixel().getGuildLeaderboardMetrics(guild.getId());
        if (metrics.getData().isEmpty()) {
            MessageFactory.makeWarning(event.getMessage(), "There are no metrics for this guild yet!\nTry again later.")
                .setTitle(guild.getName() + " Overview")
                .queue();
            return;
        }

        int page = NumberUtil.isNumeric(args[args.length - 1]) ? NumberUtil.parseInt(args[args.length - 1], 1) : 1;
        SimplePaginator<GuildMetricsResponse.GuildMetrics> paginator = new SimplePaginator<>(metrics.getData(), 7, page);

        GuildMetricsResponse.GuildMetrics weekOldMetrics = metrics.getData().get(Math.min(7, metrics.getData().size()) - 1);

        PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(),
            "The guild was last updated :time!\n\nSince last week the guild has gone up:\n> **:skills** average skill levels\n> **:slayers** average slayer XP"
        )
            .setTitle(guild.getName() + " Overview")
            .set("time", guild.getLastUpdatedAt().diffForHumans())
            .set("skills", NumberUtil.formatNicelyWithDecimals(guild.getAverageSkill() - weekOldMetrics.getAverageSkill()))
            .set("slayers", NumberUtil.formatNicelyWithDecimals(guild.getAverageSlayer() - weekOldMetrics.getAverageSlayer()));

        paginator.forEach((index, key, guildMetrics) -> {
            message.addField("Stats from " + guildMetrics.getCreatedAt().diffForHumans(), String.format(
                "```elm\nAverage Skills  > %s (%s)\nAverage Slayers > %s\nMembers         > %s```",
                NumberUtil.formatNicelyWithDecimals(guildMetrics.getAverageSkillProgress()),
                NumberUtil.formatNicelyWithDecimals(guildMetrics.getAverageSkill()),
                NumberUtil.formatNicelyWithDecimals(guildMetrics.getAverageSlayer()),
                NumberUtil.formatNicely(guildMetrics.getMembers())
            ), false);
        });

        message.addField("", "\n" + paginator.generateFooter(String.format("%sleaderboard overview %s",
            Constants.COMMAND_PREFIX, guild.getName()
        )), false);

        message.queue();
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private void showGuildLeaderboard(MessageReceivedEvent event, String[] args) {
        GuildLeaderboardResponse leaderboard = app.getHypixel().getGuildLeaderboard();

        List<String> slayerRow = new ArrayList<>();
        List<GuildLeaderboardResponse.Guild> sortedBySlayer = leaderboard.getData().stream()
            .filter(guild -> guild.getName() != null)
            .sorted((o1, o2) -> Double.compare(o2.getAverageSlayer(), o1.getAverageSlayer()))
            .collect(Collectors.toList());

        List<String> skillsRow = new ArrayList<>();
        List<GuildLeaderboardResponse.Guild> sortedBySkills = leaderboard.getData().stream()
            .filter(guild -> guild.getName() != null)
            .sorted((o1, o2) -> Double.compare(o2.getAverageSkill(), o1.getAverageSkill()))
            .collect(Collectors.toList());

        for (int i = 0; i < sortedBySkills.size(); i++) {
            GuildLeaderboardResponse.Guild skillsGuild = sortedBySkills.get(i);
            skillsRow.add(String.format("%s: %s\n    > %s (%s) < [%s]",
                padPosition("#" + (i + 1), i), skillsGuild.getName(),
                NumberUtil.formatNicelyWithDecimals(skillsGuild.getAverageSkillProgress()),
                NumberUtil.formatNicelyWithDecimals(skillsGuild.getAverageSkill()),
                skillsGuild.getMembers()
            ));

            GuildLeaderboardResponse.Guild slayerGuild = sortedBySlayer.get(i);
            slayerRow.add(String.format("%s: %s\n    > %s < [%s]",
                padPosition("#" + (i + 1), i), slayerGuild.getName(), NumberUtil.formatNicelyWithDecimals(slayerGuild.getAverageSlayer()), slayerGuild.getMembers()
            ));
        }

        int currentPage = 1;
        if (args.length > 0) {
            currentPage = NumberUtil.parseInt(args[0], 1);
        }

        SimplePaginator<String> skillsPaginator = new SimplePaginator<>(skillsRow, 10, currentPage);
        skillsRow.clear();
        skillsPaginator.forEach((index, key, val) -> skillsRow.add(val));

        SimplePaginator<String> slayerPaginator = new SimplePaginator<>(slayerRow, 10, currentPage);
        slayerRow.clear();
        slayerPaginator.forEach((index, key, val) -> slayerRow.add(val));

        MessageFactory.makeInfo(event.getMessage(), "The guild leaderboards are the total average skill and slayer stats for each guild, the stats are refreshed every 24 hours.")
            .setTitle("Guild Leaderboard")
            .setTimestamp(Carbon.now().getTime().toInstant())
            .addField("Skills Leaderboard", String.format("```elm\n%s```", String.join("\n", skillsRow)), true)
            .addField("Slayer Leaderboard", String.format("```elm\n%s```", String.join("\n", slayerRow)), true)
            .addField(EmbedBuilder.ZERO_WIDTH_SPACE, skillsPaginator.generateFooter(
                Constants.COMMAND_PREFIX + getTriggers().get(0)
            ), false)
            .queue();
    }

    private GuildLeaderboardResponse.Guild getGuildFromName(String name) {
        GuildLeaderboardResponse guildLeaderboard = app.getHypixel().getGuildLeaderboard();
        for (GuildLeaderboardResponse.Guild guild : guildLeaderboard.getData()) {
            if (name.equalsIgnoreCase(guild.getName())) {
                return guild;
            }
        }

        return null;
    }

    private String padPosition(String string, double position) {
        StringBuilder builder = new StringBuilder(string);
        while (builder.length() < 1 + String.valueOf(position).length()) {
            builder.append(" ");
        }
        return builder.toString();
    }
}
