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
import com.senither.hypixel.contracts.hypixel.PlayerStatConversionFunction;
import com.senither.hypixel.hypixel.response.GuildLeaderboardResponse;
import com.senither.hypixel.hypixel.response.PlayerLeaderboardResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
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
        if (args.length == 0) {
            showGuildLeaderboard(event);
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "You must also specify the name of the guild you wish to see the stats for."
            ).setTitle("Missing guild name").queue();
            return;
        }

        LeaderboardType type = LeaderboardType.fromName(args[0].trim());
        if (type == null) {
            List<String> types = new ArrayList<>();
            for (LeaderboardType leaderboardType : LeaderboardType.values()) {
                types.add(leaderboardType.aliases.get(0));
            }

            MessageFactory.makeError(event.getMessage(),
                "Invalid type given, please provide one of the types below to see a leaderboard for your desired guild.\n`:types`"
            ).set("types", String.join("`, `", types)).setTitle("Invalid leaderboard type").queue();
            return;
        }

        int pageNumber = 1;
        String guildName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (NumberUtil.isNumeric(args[args.length - 1])) {
            pageNumber = NumberUtil.parseInt(args[args.length - 1], 1);
            guildName = guildName.substring(0, guildName.length() - args[args.length - 1].length()).trim();
        }

        GuildLeaderboardResponse.Guild guild = getGuildFromName(guildName);
        if (guild == null) {
            MessageFactory.makeError(event.getMessage(),
                "There are no guild called `:name` that are being tracked by the bot currently, "
                    + "please provide the name of a guild that the bot is already tracking to see their player leaderboard."
            ).set("name", guildName).setTitle("Invalid guild name").queue();
            return;
        }

        PlayerLeaderboardResponse leaderboard = app.getHypixel().getGuildPlayersLeaderboard(guild.getId());
        if (leaderboard == null || !leaderboard.isSuccess()) {
            MessageFactory.makeError(event.getMessage(),
                "The `:name` guild does not appear to have any player stats being tracked right now, try again later."
            ).set("name", guildName).setTitle("Missing guild data").queue();
            return;
        }

        final int[] index = {1};
        List<String> completeRows = new ArrayList<>();
        leaderboard.getData().stream()
            .sorted((o1, o2) -> type.function.getStat(o2) > type.function.getStat(o1) ? 1 : -1)
            .forEach(player -> {
                completeRows.add(String.format("#%s : %s\n   > %s",
                    index[0]++, player.getUsername(), NumberUtil.formatNicelyWithDecimals(type.function.getStat(player))
                ));
            });

        List<String> rows = new ArrayList<>();
        SimplePaginator<String> paginator = new SimplePaginator<>(completeRows, 10, pageNumber);
        paginator.forEach((index1, key, val) -> rows.add(val));

        String command = String.format("%s%s %s %s",
            Constants.COMMAND_PREFIX, getTriggers().get(0),
            type.aliases.get(0), guild.getName()
        );

        MessageFactory.makeInfo(event.getMessage(), String.format("```ada\n%s```", String.join("\n", rows)) + "\n" + paginator.generateFooter(command))
            .setTitle(String.format("%s's %s Leaderboard", guild.getName(), type.name))
            .setTimestamp(Carbon.now().getTime().toInstant())
            .queue();
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private void showGuildLeaderboard(MessageReceivedEvent event) {
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

    private GuildLeaderboardResponse.Guild getGuildFromName(String name) {
        GuildLeaderboardResponse guildLeaderboard = app.getHypixel().getGuildLeaderboard();
        for (GuildLeaderboardResponse.Guild guild : guildLeaderboard.getData()) {
            if (guild.getName().equalsIgnoreCase(name)) {
                return guild;
            }
        }

        return null;
    }

    enum LeaderboardType {

        AVERAGE_SKILL("Average Skill", Arrays.asList("skills", "skill"), PlayerLeaderboardResponse.Player::getAverageSkill),
        TOTAL_SLAYER("Total Slayer", Arrays.asList("slayers", "slayer"), PlayerLeaderboardResponse.Player::getTotalSlayer),
        MINING("Mining", Arrays.asList("mining", "mine", "ore"), PlayerLeaderboardResponse.Player::getMining),
        FORAGING("Foraging", Arrays.asList("foraging", "forage", "tree"), PlayerLeaderboardResponse.Player::getForaging),
        ENCHANTING("Enchanting", Arrays.asList("enchanting", "enchant"), PlayerLeaderboardResponse.Player::getEnchanting),
        FARMING("Farming", Arrays.asList("farming", "farm"), PlayerLeaderboardResponse.Player::getFarming),
        COMBAT("Combat", Arrays.asList("combat", "fight"), PlayerLeaderboardResponse.Player::getCombat),
        FISHING("Fishing", Arrays.asList("fishing", "fish"), PlayerLeaderboardResponse.Player::getFishing),
        ALCHEMY("Alchemy", Arrays.asList("alchemy", "pot"), PlayerLeaderboardResponse.Player::getAlchemy),
        CARPENTRY("Carpentry", Arrays.asList("carpentry", "craft"), PlayerLeaderboardResponse.Player::getCarpentry),
        RUNECRAFTING("Runecrafting", Arrays.asList("runecrafting", "rune"), PlayerLeaderboardResponse.Player::getRunecrafting);

        protected final String name;
        protected final List<String> aliases;
        protected final PlayerStatConversionFunction function;

        LeaderboardType(String name, List<String> aliases, PlayerStatConversionFunction function) {
            this.name = name;
            this.aliases = aliases;
            this.function = function;
        }

        public static LeaderboardType fromName(String name) {
            if (name == null) {
                return null;
            }

            for (LeaderboardType leaderboardType : values()) {
                if (leaderboardType.aliases.contains(name.toLowerCase())) {
                    return leaderboardType;
                }
            }

            return null;
        }
    }
}
