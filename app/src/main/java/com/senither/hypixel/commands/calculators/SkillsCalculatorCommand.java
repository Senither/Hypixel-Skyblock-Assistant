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

import com.google.common.collect.ImmutableMultiset;
import com.google.gson.JsonObject;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.CalculatorCommand;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.DungeonResponse;
import com.senither.hypixel.statistics.responses.SkillsResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SkillsCalculatorCommand extends CalculatorCommand {

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
        return Arrays.asList("calcskills", "calcskill", "calcsk", "csk");
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
                getExperienceForLevel(Constants.GENERAL_SKILL_EXPERIENCE, max) - getExperienceForLevel(Constants.GENERAL_SKILL_EXPERIENCE, min)
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
                if (playerReply == null || playerReply.getPlayer() == null) {
                    message.editMessage(new EmbedBuilder()
                        .setColor(MessageType.ERROR.getColor())
                        .setDescription(String.format("Failed to load player data for **%s**, found no valid player data, does the user exists?",
                            username
                        ))
                        .build()
                    ).queue();
                    return;
                }

                SkyBlockProfileReply profileReply = app.getHypixel().getSelectedSkyBlockProfileFromUsername(username).get(5, TimeUnit.SECONDS);
                JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerReply.getPlayer().get("uuid").getAsString());

                SkillsResponse skillsResponse = StatisticsChecker.SKILLS.checkUser(playerReply, profileReply, member);
                DungeonResponse dungeonResponse = StatisticsChecker.DUNGEON.checkUser(playerReply, profileReply, member);

                //noinspection rawtypes
                CalculatorCommand.SkillType type = getSkillTypeFromName(args[0], skillsResponse, dungeonResponse);

                if (type == null) {
                    message.editMessage(embedBuilder
                        .setColor(MessageType.WARNING.getColor())
                        .setTitle("Invalid skill type given!")
                        .setDescription(String.join("\n", Arrays.asList(
                            "Invalid skill type provided, the skill type must be one of the following:",
                            "`mining`, `foraging`, `enchanting`, `farming`, `combat`, `fishing`, `alchemy`, "
                                + "`taming`, `catacomb`, `healer`, `mage`, `berserk`, `archer`, or `tank`"
                        )))
                        .build()
                    ).queue();
                    return;
                }

                double experience = type.getStat().getExperience() == -1
                    ? getExperienceForLevel(type.getExperienceList(), (int) type.getStat().getLevel())
                    : type.getStat().getExperience();

                int max = NumberUtil.getBetween(NumberUtil.parseInt(args[1], type.getExperienceList().size()), 0, type.getExperienceList().size());
                double levelExperience = getExperienceForLevel(type.getExperienceList(), max);
                double diff = levelExperience - experience;

                String note = "You need another **%s** XP to reach level **%s**!";
                if (diff < 0) {
                    diff = diff * -1;
                    note = "You're currently **%s** XP above level **%s**!";
                }

                if (type.getType().equals(SkillCalculationType.GENERAL)) {
                    double previousAverage = skillsResponse.getAverageSkillLevel();

                    //noinspection unchecked
                    SkillsResponse newResponse = (SkillsResponse) type.setLevelAndExperience(skillsResponse, max, levelExperience);

                    note += String.format("\nYou'll go from **%s** skill average to **%s**!",
                        NumberUtil.formatNicelyWithDecimals(previousAverage),
                        NumberUtil.formatNicelyWithDecimals(newResponse.getAverageSkillLevel())
                    );
                }

                message.editMessage(embedBuilder
                    .setTitle(type.getName() + " Skill Calculation for " + username)
                    .setDescription(String.format("You're currently %s level **%s** with **%s** XP!\n" + note,
                        type.getName(),
                        NumberUtil.formatNicelyWithDecimals(type.getStat().getLevel()),
                        NumberUtil.formatNicelyWithDecimals(experience),
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

    private double getExperienceForLevel(ImmutableMultiset<Integer> experience, int level) {
        double totalRequiredExperience = 0;
        for (int i = 0; i < Math.min(level, experience.size()); i++) {
            totalRequiredExperience += experience.asList().get(i);
        }
        return totalRequiredExperience;
    }
}
