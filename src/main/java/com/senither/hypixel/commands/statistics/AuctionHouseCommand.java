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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AuctionHouseCommand extends SkillCommand {

    public AuctionHouseCommand(SkyblockAssistant app) {
        super(app, "Auction House Statistic");
    }

    @Override
    public String getName() {
        return "Auction House Statistics";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets some general information about a users auction house activity, like",
            "how much money they have earned, the amount of bids and auction they",
            "have created, etc."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username>` - Gets AH stats for the given username",
            "`:command <mention>` - Gets AH stats for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command Senither`",
            "`:command @Senither`"
        );
    }
    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ah");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerReply.getPlayer().get("uuid").getAsString());

        JsonObject stats = member.getAsJsonObject("stats");

        message.editMessage(new EmbedBuilder()
            .setTitle(playerReply.getPlayer().get("displayname").getAsString() + "'s Auction House Statistics")
            .setDescription(String.format("**%s** has earned a total of **%s** coins through auctions.",
                playerReply.getPlayer().get("displayname").getAsString(),
                NumberUtil.formatNicely(getStatsTypeFromObject(stats, "auctions_gold_earned"))
            ))
            .setColor(MessageType.SUCCESS.getColor())
            .addField("Total Bids", NumberUtil.formatNicely(getStatsTypeFromObject(stats, "auctions_bids")), true)
            .addField("Highest Bid", NumberUtil.formatNicely(getStatsTypeFromObject(stats, "auctions_highest_bid")), true)
            .addField("Coins Spent", NumberUtil.formatNicely(getStatsTypeFromObject(stats, "auctions_gold_spent")), true)
            .addField("Auctions Won", NumberUtil.formatNicely(getStatsTypeFromObject(stats, "auctions_won")), true)
            .addField("Auctions Created", NumberUtil.formatNicely(getStatsTypeFromObject(stats, "auctions_created")), true)
            .addField("Creation Fees", NumberUtil.formatNicely(getStatsTypeFromObject(stats, "auctions_fees")), true)
            .build()
        ).queue();
    }

    private int getStatsTypeFromObject(JsonObject json, String name) {
        try {
            return json.get(name).getAsInt();
        } catch (NullPointerException e) {
            return 0;
        }
    }
}
