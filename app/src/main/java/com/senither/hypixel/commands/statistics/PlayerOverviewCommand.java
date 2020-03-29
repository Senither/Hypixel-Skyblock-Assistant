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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.rank.items.Collection;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.SlayerResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PlayerOverviewCommand extends SkillCommand {

    public PlayerOverviewCommand(SkyblockAssistant app) {
        super(app, "Overview");
    }

    @Override
    public String getName() {
        return "Player Overview Statistics";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets a list of general information about the users profile, including",
            "average skill level, slayer, minions, collections, and coins."
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
        return Arrays.asList("player", "profile", "overview");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);

        final PlaceholderMessage placeholderMessage = MessageFactory.makeSuccess(
            message, ""
        ).setTitle(getUsernameFromPlayer(playerReply) + "'s Profile Overview");

        message.editMessage(placeholderMessage
            .addField("Average Skill Level", NumberUtil.formatNicelyWithDecimals(
                StatisticsChecker.SKILLS.checkUser(playerReply, profileReply, member).getAverageSkillLevel()
            ), true)
            .addField("Collection", getCompletedCollections(profileReply, member), true)
            .addField("Pets", NumberUtil.formatNicely(member.get("pets").getAsJsonArray().size()), true)
            .addField("Minion Slots", getMinionSlots(profileReply), true)
            .addField("Coins", getCoins(profileReply, member), true)
            .addField("Slayer", getTotalSlayerXp(profileReply, member), true)
            .setFooter(String.format("Profile: %s", profileReply.getProfile().get("cute_name").getAsString()))
            .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant())
            .buildEmbed()
        ).queue();
    }

    private String getTotalSlayerXp(SkyBlockProfileReply profileReply, JsonObject member) {
        SlayerResponse response = StatisticsChecker.SLAYER.checkUser(null, profileReply, member);
        if (!response.isApiEnable() || response.getTotalSlayerExperience() == 0) {
            return "No Slayer to Display";
        }
        return NumberUtil.formatNicely(response.getTotalSlayerExperience()) + " Total XP";
    }

    private String getCoins(SkyBlockProfileReply profileReply, JsonObject member) {
        double coinsInPurse = member.get("coin_purse").getAsDouble();
        if (!profileReply.getProfile().has("banking")) {
            return NumberUtil.formatNicelyWithDecimals(coinsInPurse) + " (API is Disabled)";
        }
        return NumberUtil.formatNicelyWithDecimals(profileReply.getProfile().get("banking").getAsJsonObject().get("balance").getAsDouble() + coinsInPurse);
    }

    private String getCompletedCollections(SkyBlockProfileReply profileReply, JsonObject member) {
        if (!member.has("unlocked_coll_tiers")) {
            return "API is Disabled";
        }

        HashSet<Collection> uniqueCrafts = new HashSet<>();
        for (String profileId : profileReply.getProfile().getAsJsonObject("members").keySet()) {
            JsonObject profileMember = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(profileId);
            if (!profileMember.has("unlocked_coll_tiers")) {
                continue;
            }

            JsonArray playerUnlockedCollection = profileMember.get("unlocked_coll_tiers").getAsJsonArray();
            for (Collection collection : Collection.values()) {
                String maxTierToken = collection.getKey() + "_" + collection.getMaxLevel();

                for (JsonElement element : playerUnlockedCollection) {
                    if (element.getAsString().equals(maxTierToken)) {
                        uniqueCrafts.add(collection);
                    }
                }
            }
        }

        for (Collection collection : Collection.values()) {
            if (!uniqueCrafts.contains(collection)) {
                System.out.println(collection.name());
            }
        }

        return String.format("%s / %s", uniqueCrafts.size(), Collection.values().length);
    }

    private String getMinionSlots(SkyBlockProfileReply profileReply) {
        HashSet<String> uniqueCrafts = new HashSet<>();
        for (String profileId : profileReply.getProfile().getAsJsonObject("members").keySet()) {
            JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(profileId);
            if (!member.has("crafted_generators")) {
                continue;
            }

            for (JsonElement generator : member.get("crafted_generators").getAsJsonArray()) {
                uniqueCrafts.add(generator.getAsString());
            }
        }

        int craftedMinions = uniqueCrafts.size();
        int minionSlots = craftedMinions < 5 ? 5
            : (int) (craftedMinions < 15 ? 6
            : craftedMinions < 30 ? 7
            : craftedMinions < 50 ? 8
            : craftedMinions < 300 ? 9 + Math.floor((craftedMinions - 50) / 25)
            : 19 + Math.floor((craftedMinions - 300) / 50));

        return String.format("%s _(%s/572 Crafts)_",
            minionSlots, craftedMinions
        );
    }
}
