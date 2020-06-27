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
import com.senither.hypixel.hypixel.HypixelRank;
import com.senither.hypixel.rank.items.Collection;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.SkillsResponse;
import com.senither.hypixel.statistics.responses.SlayerResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlayerOverviewCommand extends SkillCommand {

    private static final Logger log = LoggerFactory.getLogger(PlayerOverviewCommand.class);

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
        return Arrays.asList("player", "profile", "overview", "stats", "p");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, String[] args) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);
        HypixelRank hypixelRank = app.getHypixel().getRankFromPlayer(playerReply);

        String embeddedMessage = hypixelRank.equals(HypixelRank.DEFAULT)
            ? "**:name** does not have any rank on Hypixel"
            : "**:name** has the **:rank** rank on Hypixel";

        String guildName = getGuildNameForPlayer(playerReply.getPlayer().get("uuid").getAsString());
        if (guildName != null) {
            embeddedMessage += ", and is a member of **:guild**";
        }

        final PlaceholderMessage placeholderMessage = MessageFactory.makeSuccess(
            message, embeddedMessage + "!"
        )
            .set("name", playerReply.getPlayer().get("displayname").getAsString())
            .set("rank", hypixelRank.getName())
            .set("guild", guildName)
            .setTitle(getUsernameFromPlayer(playerReply) + "'s Profile Overview");

        SkillsResponse skillsResponse = StatisticsChecker.SKILLS.checkUser(playerReply, profileReply, member);

        message.editMessage(placeholderMessage
            .addField("Average Skill Level", String.format("%s [%s w/o progress]",
                NumberUtil.formatNicelyWithDecimals(skillsResponse.getAverageSkillLevel()),
                NumberUtil.formatNicelyWithDecimals(skillsResponse.getAverageSkillLevelWithoutPorgress())
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

    private String getGuildNameForPlayer(String uuid) {
        try {
            GuildReply guild = app.getHypixel().getGuildByPlayer(uuid).get(5, TimeUnit.SECONDS);
            if (guild == null || guild.getGuild() == null) {
                return null;
            }
            return guild.getGuild().getName();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to fetch guild from player {} due to an exception: {}", uuid, e.getMessage(), e);

            return null;
        }
    }
}
