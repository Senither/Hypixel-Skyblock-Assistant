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

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.SlayerResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SlayerCommand extends SkillCommand {

    private static final Logger log = LoggerFactory.getLogger(SlayerCommand.class);

    public SlayerCommand(SkyblockAssistant app) {
        super(app, "slayer");
    }

    @Override
    public String getName() {
        return "Slayer Statistics";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets a players slayer statistics, like how many times they have",
            "killed each boss, their slayer levels, and how many coins they",
            "have spent on slayer quests in total."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username> [profile]` - Gets slayer stats for the given username",
            "`:command <mention> [profile]` - Gets slayer stats for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("slayers", "slayer");
    }

    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);

        SlayerResponse response = StatisticsChecker.SLAYER.checkUser(playerReply, profileReply, member);
        if (!response.isApiEnable()) {
            message.editMessage(MessageFactory.makeEmbeddedMessage(message.getChannel())
                .setTitle(getUsernameFromPlayer(playerReply) + "'s Slayers")
                .setDescription(getUsernameFromPlayer(playerReply) + " haven't done any slayer quests on their :profile profile yet, so there is nothing to display!")
                .setColor(MessageType.WARNING.getColor())
                .set("profile", profileReply.getProfile().get("cute_name").getAsString())
                .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant())
                .buildEmbed()
            ).queue();
            return;
        }
        message.editMessage(new EmbedBuilder()
            .setTitle(getUsernameFromPlayer(playerReply) + "'s Slayers")
            .setDescription(String.format("%s has spent %s coins on slayer quests.",
                getUsernameFromPlayer(playerReply),
                NumberUtil.formatNicely(response.getTotalCoinsSpent())
            ))
            .setColor(MessageType.SUCCESS.getColor())
            .addField("Revenant Horror", buildSlayerStatsFromType(response.getRevenant()), true)
            .addField("Tarantula Broodfather", buildSlayerStatsFromType(response.getTarantula()), true)
            .addField("Sven Packmaster", buildSlayerStatsFromType(response.getSven()), true)
            .setFooter(String.format("%s has a total of %s Slayer experience. | Profile: %s",
                getUsernameFromPlayer(playerReply),
                NumberUtil.formatNicely(response.getTotalCoinsSpent()),
                profileReply.getProfile().get("cute_name").getAsString()
            ))
            .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant())
            .build()
        ).queue();
    }

    private String buildSlayerStatsFromType(SlayerResponse.SlayerStat stats) {
        return new PlaceholderMessage(null, String.join("\n", Arrays.asList(
            "**Tier 1:** :boss1",
            "**Tier 2:** :boss2",
            "**Tier 3:** :boss3",
            "**Tier 4:** :boss4",
            "**EXP:** :experience",
            "**LvL:** :level"
        )))
            .set("boss1", NumberUtil.formatNicely(stats.getTier1Kills()))
            .set("boss2", NumberUtil.formatNicely(stats.getTier2Kills()))
            .set("boss3", NumberUtil.formatNicely(stats.getTier3Kills()))
            .set("boss4", NumberUtil.formatNicely(stats.getTier4Kills()))
            .set("experience", NumberUtil.formatNicely(stats.getExperience()))
            .set("level", NumberUtil.formatNicelyWithDecimals(stats.getLevelFromExperience()))
            .toString();
    }
}
