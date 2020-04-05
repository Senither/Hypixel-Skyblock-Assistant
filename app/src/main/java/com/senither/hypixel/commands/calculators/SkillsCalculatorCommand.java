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

package com.senither.hypixel.commands.calculators;

import com.google.gson.JsonObject;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.SkillsResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SkillsCalculatorCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(SkillsCalculatorCommand.class);

    public SkillsCalculatorCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Skills Calculator";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Calculates the needed XP between two levels, or the XP required to reach a given level."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <to> <from>` - Calculates the required XP using the given levels.",
            "`:command <skill> <level>` - Calculates the XP needed to reach the given level from your current skill XP."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 3 45` - Calculates the XP needed to go from level 3 to 45.",
            "`:command combat 50` - Calculates the amount of XP you need to reach combat level 50."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("calcskill", "calcskills");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must specify the minimum level or skill type you want the calculate the level different for."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "You must specify the maximum level so skill difference can be calculated."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (NumberUtil.isNumeric(args[0])) {
            handleCalculateSkillFromLevel(event, args);
        } else {
            handleCalculateSkillFromSkill(event, args);
        }
    }

    private void handleCalculateSkillFromLevel(MessageReceivedEvent event, String[] args) {
        int min = NumberUtil.getBetween(NumberUtil.parseInt(args[0], 0), 0, Constants.GENERAL_SKILL_EXPERIENCE.size());
        int max = NumberUtil.getBetween(NumberUtil.parseInt(args[1], 50), 0, Constants.GENERAL_SKILL_EXPERIENCE.size());

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        MessageFactory.makeSuccess(event.getMessage(), "You need **:xp** XP to go from level **:first** to **:second**!")
            .setTitle("Skill Calculation")
            .set("xp", NumberUtil.formatNicelyWithDecimals(
                getExperienceForLevel(max) - getExperienceForLevel(min)
            ))
            .set("first", min)
            .set("second", max)
            .queue();
    }

    private void handleCalculateSkillFromSkill(MessageReceivedEvent event, String[] args) {
        String username = getUsernameFromUser(event.getAuthor());
        if (username == null) {
            MessageFactory.makeError(event.getMessage(), "Failed to load your minecraft username, please try again later.").queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle("Loading Skyblock profile data for " + username + "!")
            .setDescription("Loading Skyblock profile data for " + username + "!")
            .setColor(MessageType.INFO.getColor());

        event.getChannel().sendMessage(embedBuilder.build()).queue(message -> {
            try {
                PlayerReply playerReply = app.getHypixel().getPlayerByName(username).get(5, TimeUnit.SECONDS);
                if (playerReply == null) {
                    message.editMessage(new EmbedBuilder()
                        .setColor(MessageType.ERROR.getColor())
                        .setDescription(String.format("Failed to load player data for **%s**, found no valid player data.",
                            username
                        ))
                        .build()
                    ).queue();
                    return;
                }

                SkyBlockProfileReply profileReply = app.getHypixel().getSelectedSkyBlockProfileFromUsername(username).get(5, TimeUnit.SECONDS);
                JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerReply.getPlayer().get("uuid").getAsString());

                String skillType = null;
                SkillsResponse.SkillStat stat = null;
                SkillsResponse response = StatisticsChecker.SKILLS.checkUser(playerReply, profileReply, member);

                switch (args[0].toLowerCase()) {
                    case "mining":
                        skillType = "Mining";
                        stat = response.getMining();
                        break;
                    case "foraging":
                        skillType = "Foraging";
                        stat = response.getForaging();
                        break;
                    case "enchanting":
                        skillType = "Enchanting";
                        stat = response.getEnchanting();
                        break;
                    case "farming":
                        skillType = "Farming";
                        stat = response.getFarming();
                        break;
                    case "combat":
                        skillType = "Combat";
                        stat = response.getCombat();
                        break;
                    case "fishing":
                        skillType = "Fishing";
                        stat = response.getFishing();
                        break;
                    case "alchemy":
                        skillType = "Alchemy";
                        stat = response.getAlchemy();
                        break;
                }

                if (stat == null) {
                    message.editMessage(embedBuilder
                        .setColor(MessageType.WARNING.getColor())
                        .setTitle("Invalid skill type given!")
                        .setDescription(String.join("\n", Arrays.asList(
                            "Invalid skill type provided, the skill type must be one of the following:",
                            "`mining`, `foraging`, `enchanting`, `farming`, `combat`, `fishing`, or `alchemy`"
                        )))
                        .build()
                    ).queue();
                    return;
                }

                double experience = stat.getExperience() == -1 ? getExperienceForLevel((int) stat.getLevel()) : stat.getExperience();
                int max = NumberUtil.getBetween(NumberUtil.parseInt(args[1], 50), 0, Constants.GENERAL_SKILL_EXPERIENCE.size());
                double diff = getExperienceForLevel(max) - experience;

                String note = "You need another **%s** XP to reach level **%s**!";
                if (diff < 0) {
                    diff = diff * -1;
                    note = "You're currently **%s** XP above level **%s**!";
                }

                message.editMessage(embedBuilder
                    .setTitle(skillType + " Skill Calculation for " + username)
                    .setDescription(String.format("You're currently %s level **%s** with **%s** XP!\n" + note,
                        skillType,
                        (int) stat.getLevel(), NumberUtil.formatNicelyWithDecimals(experience),
                        NumberUtil.formatNicelyWithDecimals(diff), max
                    ))
                    .setColor(MessageType.SUCCESS.getColor())
                    .setFooter(String.format("Profile: %s", profileReply.getProfile().get("cute_name").getAsString()))
                    .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant())
                    .build()
                ).queue();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                sendExceptionMessage(message, embedBuilder, e);
            }
        });
    }

    private double getExperienceForLevel(int level) {
        double totalRequiredExperience = 0;
        for (int i = 0; i < Math.min(level, Constants.GENERAL_SKILL_EXPERIENCE.size()); i++) {
            totalRequiredExperience += Constants.GENERAL_SKILL_EXPERIENCE.asList().get(i);
        }
        return totalRequiredExperience;
    }
}
