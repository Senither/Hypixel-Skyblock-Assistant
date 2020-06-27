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
import com.senither.hypixel.hypixel.leaderboard.LeaderboardPlayer;
import com.senither.hypixel.hypixel.leaderboard.LeaderboardType;
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

public class PlayerLeaderboardCommand extends Command {

    public PlayerLeaderboardCommand(SkyblockAssistant app) {
        super(app, false);
    }

    @Override
    public String getName() {
        return "Player Leaderboard";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to show a list of players that are in guilds that are being tracked",
            "by the bot, ordered in descending order by the guild average skill level and slayer XP.\n",
            "The command also supports skill and slayer leaderboards, allowing you to see skill",
            "levels and XP for each skill type, or slayer leaderboards for each slayer type."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Lists all the tracked players",
            "`:command <type> [page]` - Shows a leaderboard with the given type"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command`",
            "`:command skills`",
            "`:command slayer 2`",
            "`:command farming 109`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("playerleaderboard", "pleaderboard", "plead", "ptop", "pt", "pl");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0 || NumberUtil.isNumeric(args[0])) {
            showLeaderboard(event, args);
            return;
        }

        LeaderboardType type = LeaderboardType.fromName(args[0].trim());
        if (type == null || LeaderboardType.OVERVIEW.equals(type)) {
            List<String> types = new ArrayList<>();
            for (LeaderboardType leaderboardType : LeaderboardType.values()) {
                if (LeaderboardType.OVERVIEW.equals(leaderboardType)) {
                    continue;
                }
                types.add(leaderboardType.getAliases().get(0));
            }

            MessageFactory.makeError(event.getMessage(),
                "Invalid type given, please provide one of the types below to see the player leaderboard for the given type.\n`:types`"
            ).set("types", String.join("`, `", types)).setTitle("Invalid leaderboard type").queue();
            return;
        }

        int pageNumber = 1;
        if (args.length != 1 && NumberUtil.isNumeric(args[1])) {
            pageNumber = NumberUtil.parseInt(args[args.length - 1], 1);
        }

        UUID userUUID = null;
        try {
            userUUID = app.getHypixel().getUUIDFromUser(event.getAuthor());
        } catch (SQLException ignored) {
        }

        final String rowMessage = type.getExpFunction() == null
            ? "%s: %s [%s]\n%s> %s " : type.equals(LeaderboardType.AVERAGE_SKILL)
            ? "%s: %s [%s]\n%s> %s (%s)" : "%s: %s [%s]\n%s> %s [%s XP]";

        final int[] index = {1};
        final int[] position = {-1};
        final UUID finalUserUUID = userUUID;

        List<String> completeRows = new ArrayList<>();
        app.getHypixel().getPlayerLeaderboard().getData().stream()
            .sorted((o1, o2) -> Double.compare(type.getOrderFunction().getStat(o2), type.getOrderFunction().getStat(o1)))
            .forEach(player -> {
                if (player.getUuid().equals(finalUserUUID)) {
                    position[0] = index[0];
                }

                completeRows.add(String.format(rowMessage,
                    padPosition("#" + NumberUtil.formatNicely(index[0]), index[0] - 1),
                    player.getUsername(),
                    player.getGuildName(),
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

        String command = String.format("%s%s %s",
            Constants.COMMAND_PREFIX, getTriggers().get(0),
            type.getAliases().get(0)
        );

        String note = "";
        if (finalUserUUID != null && position[0] > 0) {
            note = String.format("> You're ranked **#%s** in %s!\n\n",
                NumberUtil.formatNicely(position[0]), type.getName()
            );
        }

        MessageFactory.makeInfo(event.getMessage(), String.format(
            "```ada\n%s```",
            String.join("\n", rows)) + "\n"
            + note + paginator.generateFooter(command)
        )
            .setTitle(String.format("%s Player Leaderboard", type.getName()))
            .setFooter("Requested by " + event.getAuthor().getAsTag(), event.getAuthor().getEffectiveAvatarUrl())
            .setTimestamp(Carbon.now().getTime().toInstant())
            .queue();
    }

    private void showLeaderboard(MessageReceivedEvent event, String[] args) {
        PlayerLeaderboardResponse leaderboard = app.getHypixel().getPlayerLeaderboard();

        List<String> slayerRow = new ArrayList<>();
        List<LeaderboardPlayer> sortedBySlayer = leaderboard.getData().stream()
            .filter(guild -> guild.getGuildName() != null)
            .sorted((o1, o2) -> Double.compare(o2.getTotalSlayer(), o1.getTotalSlayer()))
            .collect(Collectors.toList());

        List<String> skillsRow = new ArrayList<>();
        List<LeaderboardPlayer> sortedBySkills = leaderboard.getData().stream()
            .filter(guild -> guild.getGuildName() != null)
            .sorted((o1, o2) -> Double.compare(o2.getAverageSkillProgress(), o1.getAverageSkillProgress()))
            .collect(Collectors.toList());

        for (int i = 0; i < sortedBySkills.size(); i++) {
            LeaderboardPlayer skillsPlayer = sortedBySkills.get(i);
            skillsRow.add(String.format("%s: %s\n%s> %s (%s)",
                padPosition("#" + NumberUtil.formatNicely(i + 1), i), skillsPlayer.getUsername(),
                padPosition("", i),
                NumberUtil.formatNicelyWithDecimals(skillsPlayer.getAverageSkillProgress()),
                NumberUtil.formatNicelyWithDecimals(skillsPlayer.getAverageSkill())
            ));

            LeaderboardPlayer slayerPlayer = sortedBySlayer.get(i);
            slayerRow.add(String.format("%s: %s\n%s> %s",
                padPosition("#" + NumberUtil.formatNicely(i + 1), i),
                slayerPlayer.getUsername(),
                padPosition("", i),
                NumberUtil.formatNicelyWithDecimals(slayerPlayer.getTotalSlayer())
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

        MessageFactory.makeInfo(event.getMessage(),
            "The players leaderboards are the total average skill and slayer stats for every player in a guild the bot is tracking, the stats are refreshed every 24 hours."
        )
            .setTitle("Player Leaderboard")
            .setTimestamp(Carbon.now().getTime().toInstant())
            .addField("Skills Leaderboard", String.format("```ada\n%s```", String.join("\n", skillsRow)), true)
            .addField("Slayer Leaderboard", String.format("```ada\n%s```", String.join("\n", slayerRow)), true)
            .addField(EmbedBuilder.ZERO_WIDTH_SPACE, skillsPaginator.generateFooter(
                Constants.COMMAND_PREFIX + getTriggers().get(0)
            ), false)
            .queue();
    }

    private String padPosition(String string, double position) {
        StringBuilder builder = new StringBuilder(string);
        while (builder.length() < 1 + String.valueOf(position).length()) {
            builder.append(" ");
        }
        return builder.toString();
    }
}
