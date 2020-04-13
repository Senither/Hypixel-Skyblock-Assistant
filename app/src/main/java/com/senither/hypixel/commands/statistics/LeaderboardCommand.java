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

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.hypixel.response.GuildLeaderboardResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
            "ordered in descending order by the guild average skill level and slayer XP."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Lists all the tracked guilds");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList(
            "leaderboard", "lead", "top"
        );
    }

    @Override
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    public void onCommand(MessageReceivedEvent event, String[] args) {
        GuildLeaderboardResponse leaderboard = app.getHypixel().getGuildLeaderboard();

        List<String> slayerRow = new ArrayList<>();
        List<GuildLeaderboardResponse.Guild> sortedBySlayer = leaderboard.getData().stream()
            .filter(guild -> guild.getName() != null)
            .sorted((o1, o2) -> o2.getAverageSlayer() > o1.getAverageSlayer() ? 1 : -1)
            .collect(Collectors.toList());

        List<String> skillsRow = new ArrayList<>();
        List<GuildLeaderboardResponse.Guild> sortedBySkills = leaderboard.getData().stream()
            .filter(guild -> guild.getName() != null)
            .sorted((o1, o2) -> o2.getAverageSkill() > o1.getAverageSkill() ? 1 : -1)
            .collect(Collectors.toList());


        for (int i = 0; i < sortedBySkills.size(); i++) {
            GuildLeaderboardResponse.Guild skillsGuild = sortedBySkills.get(i);
            skillsRow.add(String.format("#%s : %s [%s]\n   > %s",
                i + 1, skillsGuild.getName(), skillsGuild.getMembers(), NumberUtil.formatNicelyWithDecimals(skillsGuild.getAverageSkill())
            ));

            GuildLeaderboardResponse.Guild slayerGuild = sortedBySlayer.get(i);
            slayerRow.add(String.format("#%s : %s [%s]\n   > %s",
                i + 1, slayerGuild.getName(), slayerGuild.getMembers(), NumberUtil.formatNicelyWithDecimals(slayerGuild.getAverageSlayer())
            ));
        }

        MessageFactory.makeInfo(event.getMessage(), "The guild leaderboards are the total average skill and slayer stats for each guild, the stats are refreshed every 24 hours.")
            .setTitle("Guild Leaderboard")
            .setTimestamp(Carbon.now().getTime().toInstant())
            .addField("Skills Leaderboard", String.format("```elm\n%s```", String.join("\n", skillsRow)), true)
            .addField("Slayer Leaderboard", String.format("```elm\n%s```", String.join("\n", slayerRow)), true)
            .queue();
    }
}
