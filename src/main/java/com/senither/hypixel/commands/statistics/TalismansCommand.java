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
import com.senither.hypixel.inventory.Inventory;
import com.senither.hypixel.inventory.Item;
import com.senither.hypixel.inventory.ItemRarity;
import com.senither.hypixel.inventory.ItemType;
import com.senither.hypixel.time.Carbon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TalismansCommand extends SkillCommand {

    public TalismansCommand(SkyblockAssistant app) {
        super(app, "talisman");
    }

    @Override
    public String getName() {
        return "Talismans";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets a players talismans and displays them per rarity type.",
            "\n\n**Note:** This command will only work for users who have enabled their inventory API."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username> [profile]` - Gets all talismans for the given username",
            "`:command <mention> [profile]` - Gets all talismans for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("talismans", "talisman");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);

        if (!isInventoryApiEnabled(member)) {
            message.editMessage(new EmbedBuilder()
                .setColor(MessageType.WARNING.getColor())
                .setTitle("Failed to load profile!")
                .setDescription(String.format(
                    "%s has their inventory API disabled for their %s profile!\nYou can ask them nicely to enable it.",
                    getUsernameFromPlayer(playerReply),
                    profileReply.getProfile().get("cute_name").getAsString()
                ))
                .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant())
                .build()
            ).queue();
            return;
        }

        try {
            List<Item> talismans = new ArrayList<>();
            talismans.addAll(getTalismansFromInventory(member, "ender_chest_contents"));
            talismans.addAll(getTalismansFromInventory(member, "talisman_bag"));
            talismans.addAll(getTalismansFromInventory(member, "inv_contents"));

            EmbedBuilder builder = new EmbedBuilder()
                .setColor(MessageType.SUCCESS.getColor())
                .setTitle(getUsernameFromPlayer(playerReply) + "'s Talismans")
                .setDescription(String.format(
                    "**%s** has a total of **%s** talismans!",
                    getUsernameFromPlayer(playerReply),
                    talismans.size()
                ))
                .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant());

            for (ItemRarity itemRarity : ItemRarity.values()) {
                if (itemRarity.isDefault()) {
                    continue;
                }

                ArrayList<String> itemsOfRarity = new ArrayList<>();
                for (Item talisman : talismans) {
                    if (talisman.getRarity() == itemRarity) {
                        itemsOfRarity.add(talisman.getName());
                    }
                }

                if (itemsOfRarity.isEmpty()) {
                    continue;
                }

                builder.addField(String.format("%s (%S)",
                    itemRarity.getName(), itemsOfRarity.size()
                ), "`" + String.join("`, `", itemsOfRarity) + "`", false);
            }

            message.editMessage(builder.build()).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Item> getTalismansFromInventory(JsonObject member, String inventoryName) throws IOException {
        Inventory inventory = new Inventory(member.get(inventoryName).getAsJsonObject().get("data").getAsString());

        return inventory.getItemsWithType(ItemType.ACCESSORY);
    }

    private boolean isInventoryApiEnabled(JsonObject json) {
        return json.has("ender_chest_contents")
            && json.has("talisman_bag")
            && json.has("inv_contents");
    }
}
