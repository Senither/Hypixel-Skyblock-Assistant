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
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.SkillCommand;
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

    private final List<Integer> slayerLevels = Arrays.asList(
        5, 15, 200, 1000, 10000, 20000, 100000, 400000
    );

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
    public List<String> getTriggers() {
        return Arrays.asList("slayers", "slayer");
    }

    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerReply.getPlayer().get("uuid").getAsString());

        JsonObject slayerBosses = member.getAsJsonObject("slayer_bosses");
        int totalCoinsSpentOnSlayers = getTotalCoinsSpentOnSlayers(slayerBosses);
        if (totalCoinsSpentOnSlayers == 0) {
            message.editMessage(new EmbedBuilder()
                .setTitle(playerReply.getPlayer().get("displayname").getAsString() + "'s Slayers")
                .setDescription(playerReply.getPlayer().get("displayname").getAsString() + " haven't done any slayer quests yet, so there is nothing to display!")
                .setColor(MessageType.WARNING.getColor())
                .build()
            ).queue();
            return;
        }

        message.editMessage(new EmbedBuilder()
            .setTitle(playerReply.getPlayer().get("displayname").getAsString() + "'s Slayers")
            .setDescription(String.format("%s has spent %s coins on slayer quests.",
                playerReply.getPlayer().get("displayname").getAsString(),
                NumberUtil.formatNicely(totalCoinsSpentOnSlayers)
            ))
            .setColor(MessageType.SUCCESS.getColor())
            .addField("Revenant Horror", buildSlayerStatsFromType(slayerBosses.getAsJsonObject("zombie")), true)
            .addField("Tarantula Broodfather", buildSlayerStatsFromType(slayerBosses.getAsJsonObject("spider")), true)
            .addField("Sven Packmaster", buildSlayerStatsFromType(slayerBosses.getAsJsonObject("wolf")), true)
            .build()
        ).queue();
    }

    private String buildSlayerStatsFromType(JsonObject json) {
        return String.format(String.join("\n", Arrays.asList(
            "**Tier 1:** %s",
            "**Tier 2:** %s",
            "**Tier 3:** %s",
            "**Tier 4:** %s",
            "**EXP:** %s",
            "**LvL:** %s"
            )),
            NumberUtil.formatNicely(getEntryFromSlayerData(json, "boss_kills_tier_0")),
            NumberUtil.formatNicely(getEntryFromSlayerData(json, "boss_kills_tier_1")),
            NumberUtil.formatNicely(getEntryFromSlayerData(json, "boss_kills_tier_2")),
            NumberUtil.formatNicely(getEntryFromSlayerData(json, "boss_kills_tier_3")),
            NumberUtil.formatNicely(getEntryFromSlayerData(json, "xp")),
            NumberUtil.formatNicelyWithDecimals(getSlayerLevelFromExperience(getEntryFromSlayerData(json, "xp")))
        );
    }

    private int getLevelFromExperience(JsonObject jsonObject) {
        for (int i = 9; i > 0; i--) {
            if (jsonObject.has("level_" + i) && jsonObject.get("level_" + i).getAsBoolean()) {
                return i;
            }
        }
        return 0;
    }

    private int getTotalCoinsSpentOnSlayers(JsonObject jsonObject) {
        try {
            int totalCoins = 0;

            for (String type : jsonObject.keySet()) {
                JsonObject slayerType = jsonObject.getAsJsonObject(type);
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_0") * 100;
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_1") * 2000;
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_2") * 10000;
                totalCoins += getEntryFromSlayerData(slayerType.getAsJsonObject(), "boss_kills_tier_3") * 50000;
            }

            return totalCoins;
        } catch (Exception e) {
            log.error("Exception were thrown while getting total cost, error: {}", e.getMessage(), e);
            return 0;
        }
    }

    private double getSlayerLevelFromExperience(int experience) {
        double level = 0;
        for (int requirement : slayerLevels) {
            if (experience < requirement) {
                level += (double) experience / (double) requirement;
                break;
            }
            level++;
        }
        return level;
    }

    private int getEntryFromSlayerData(JsonObject jsonpObject, String entry) {
        try {
            return jsonpObject.get(entry).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }
}
