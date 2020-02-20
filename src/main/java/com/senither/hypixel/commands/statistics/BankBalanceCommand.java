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
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.Arrays;
import java.util.List;

public class BankBalanceCommand extends SkillCommand {

    public BankBalanceCommand(SkyblockAssistant app) {
        super(app, "Bank Balance");
    }

    @Override
    public String getName() {
        return "Bank Balance";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Displays a users bank and purse balance.",
            "\n\n**Note:** This command will only work for users who have enabled their bank API."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username> [profile]` - Gets bank and purse balance for the given username",
            "`:command <mention> [profile]` - Gets bank and purse balance for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("bank", "balance", "bal");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);

        double coinsInPurse = member.get("coin_purse").getAsDouble();

        if (!profileReply.getProfile().has("banking")) {
            message.editMessage(MessageFactory.makeSuccess(message, "**:name** has **:coins** in their purse.")
                .setTitle(getUsernameFromPlayer(playerReply) + "'s Bank Balance | API is Disabled")
                .setFooter(String.format("Profile: %s", profileReply.getProfile().get("cute_name").getAsString()))
                .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant())
                .set("name", getUsernameFromPlayer(playerReply))
                .set("coins", NumberUtil.formatNicelyWithDecimals(coinsInPurse))
                .buildEmbed()
            ).queue();
            return;
        }

        double coinsInBank = profileReply.getProfile().get("banking").getAsJsonObject().get("balance").getAsDouble();

        message.editMessage(MessageFactory.makeSuccess(message, "**:name** has a total of **:total** coins!")
            .setTitle(getUsernameFromPlayer(playerReply) + "'s Bank Balance")
            .addField("Bank", NumberUtil.formatNicelyWithDecimals(coinsInBank), true)
            .addField("Purse", NumberUtil.formatNicelyWithDecimals(coinsInPurse), true)
            .setFooter(String.format("Profile: %s", profileReply.getProfile().get("cute_name").getAsString()))
            .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant())
            .set("name", getUsernameFromPlayer(playerReply))
            .set("total", NumberUtil.formatNicelyWithDecimals(coinsInBank + coinsInPurse))
            .buildEmbed()
        ).queue();
    }
}
