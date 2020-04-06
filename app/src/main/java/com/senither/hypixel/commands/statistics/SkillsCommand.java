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

package com.senither.hypixel.commands.statistics;

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.SkillsResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.Arrays;
import java.util.List;

public class SkillsCommand extends SkillCommand {

    public SkillsCommand(SkyblockAssistant app) {
        super(app, "skill");
    }

    @Override
    public String getName() {
        return "Skills Statistics";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets a players average skill level, as-well-as all their skill experience and levels.",
            "\n\n**Note:** This command will only work for users who have enabled their skill API."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username> [profile]` - Gets skill stats for the given username",
            "`:command <mention> [profile]` - Gets skill stats for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("skills", "skill");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);
        String displayName = getUsernameFromPlayer(playerReply);

        SkillsResponse skillsResponse = StatisticsChecker.SKILLS.checkUser(playerReply, profileReply, member);
        if (!skillsResponse.hasData()) {
            sendAPIIsDisabledMessage(message, profileReply, displayName);
            return;
        }

        String embedMessage = String.format("**%s** has an average skill level of **%s**",
            getUsernameFromPlayer(playerReply), NumberUtil.formatNicelyWithDecimals(
                skillsResponse.getAverageSkillLevel()
            )
        );

        if (skillsResponse.isApiEnable()) {
            embedMessage += String.format(", or **%s** without skill progress",
                NumberUtil.formatNicelyWithDecimals(
                    skillsResponse.getAverageSkillLevelWithoutPorgress()
                )
            );
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle(getUsernameFromPlayer(playerReply) + "'s Skills")
            .setDescription(embedMessage + ".")
            .setColor(MessageType.SUCCESS.getColor())
            .addField("Mining", formatStatTextValue(skillsResponse.getMining()), true)
            .addField("Foraging", formatStatTextValue(skillsResponse.getForaging()), true)
            .addField("Enchanting", formatStatTextValue(skillsResponse.getEnchanting()), true)
            .addField("Farming", formatStatTextValue(skillsResponse.getFarming()), true)
            .addField("Combat", formatStatTextValue(skillsResponse.getCombat()), true)
            .addField("Fishing", formatStatTextValue(skillsResponse.getFishing()), true)
            .addField("Alchemy", formatStatTextValue(skillsResponse.getAlchemy()), true)
            .addField("Carpentry", formatStatTextValue(skillsResponse.getCarpentry()), true)
            .addField("Runecrafting", formatStatTextValue(skillsResponse.getRunecrafting()), true)
            .setFooter(String.format(
                "Note > Carpentry and Runecrafting are cosmetic skills, and are therefore not included in the average skill calculation. | Profile: %s",
                profileReply.getProfile().get("cute_name").getAsString()
            ))
            .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant());

        if (!skillsResponse.isApiEnable()) {
            embedBuilder
                .setTitle(displayName + "'s Skills | API is Disabled")
                .setFooter(MessageFactory.makeInfo(message, String.join(" ",
                    "Note > The skills API is disabled, so these skills are pulled",
                    "from the Skyblock Skills achievements instead, which means the displayed skills above might not be 100%",
                    "accurate for the selected profile. | Profile: :profile"
                )).set("name", displayName).set("profile", profileReply.getProfile().get("cute_name").getAsString()).toString());
        }

        message.editMessage(embedBuilder.build()).queue();
    }

    private String formatStatTextValue(SkillsResponse.SkillStat stat) {
        if (stat.getLevel() < 0) {
            return "Unknown";
        }

        String formattedStat = "**LvL:** " + NumberUtil.formatNicelyWithDecimals(stat.getLevel());
        if (stat.getExperience() <= 0) {
            return formattedStat;
        }
        return formattedStat + "\n**EXP:** " + NumberUtil.formatNicely(stat.getExperience());
    }

    private void sendAPIIsDisabledMessage(Message message, SkyBlockProfileReply profileReply, String username) {
        message.editMessage(new EmbedBuilder()
            .setColor(MessageType.WARNING.getColor())
            .setTitle("Failed to load profile!")
            .setDescription(String.format(
                "%s has their skills API disabled for their %s profile!\nYou can ask them nicely to enable it.",
                username, profileReply.getProfile().get("cute_name").getAsString()
            ))
            .build()
        ).queue();
    }
}
