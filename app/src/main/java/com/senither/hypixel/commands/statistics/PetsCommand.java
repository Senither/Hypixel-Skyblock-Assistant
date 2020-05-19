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
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.PetsResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.*;

public class PetsCommand extends SkillCommand {

    public PetsCommand(SkyblockAssistant app) {
        super(app, "Pet");
    }

    @Override
    public String getName() {
        return "Pets Command";
    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList(
            "Displays a list of a users pets, and their selected pet, including level, progress, and rarity"
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username> [profile]` - Gets pets for the given username",
            "`:command <mention> [profile]` - Gets pets for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("pets", "pet");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, String[] args) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);
        PetsResponse response = StatisticsChecker.PETS.checkUser(playerReply, profileReply, member);

        if (!hasResponseData(message, profileReply, playerReply, response)) {
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
            .setColor(MessageType.SUCCESS.getColor())
            .setTitle(message.getEmbeds().get(0).getTitle())
            .setDescription(String.format(
                "**%s** doesn't have any pet active right now.",
                getUsernameFromPlayer(playerReply)
            ))
            .setFooter(String.format(
                "Profile: %s",
                profileReply.getProfile().get("cute_name").getAsString()
            ))
            .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant());

        if (response.getActivePet() != null) {
            PetsResponse.Pet activePet = response.getActivePet();
            builder.setDescription(String.format(
                "**%s** is currently using a **%s**.",
                getUsernameFromPlayer(playerReply),
                activePet.toFormattedString()
            ));
        }

        List<String> otherPets = new ArrayList<>();
        response.getPets().stream()
            .sorted(Comparator.comparingLong(PetsResponse.Pet::getExperience).reversed())
            .forEach(pet -> {
                if (pet.isActive()) {
                    return;
                }
                otherPets.add(pet.toFormattedString());
            });


        if (!otherPets.isEmpty()) {
            int page = 1;
            if (args.length > 0) {
                page = NumberUtil.parseInt(args[0], 1);
            }

            SimplePaginator<String> paginator = new SimplePaginator<>(otherPets, 25, page);

            List<String> pagePets = new ArrayList<>();
            paginator.forEach((index, key, val) -> pagePets.add(val));

            builder.addField(String.format("Other Pets (%s in total)",
                otherPets.size() + (response.getActivePet() == null ? 0 : 1)
            ), String.format("```php\n%s```", String.join(
                "\n", pagePets
            )), false);

            builder.addField("", paginator.generateFooter(
                Constants.COMMAND_PREFIX + getTriggers().get(0) + " " + playerReply.getPlayer().get("displayname").getAsString()
            ), false);
        }

        message.editMessage(builder.build()).queue();
    }

    private boolean hasResponseData(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, PetsResponse response) {
        if (!response.isApiEnable()) {
            message.editMessage(new EmbedBuilder()
                .setColor(MessageType.WARNING.getColor())
                .setTitle("Failed to load profile!")
                .setDescription(String.format(
                    "%s has their pets API disabled for their %s profile!\nYou can ask them nicely to enable it.",
                    getUsernameFromPlayer(playerReply), profileReply.getProfile().get("cute_name").getAsString()
                ))
                .build()
            ).queue();
            return false;
        }

        if (!response.hasData()) {
            message.editMessage(new EmbedBuilder()
                .setColor(MessageType.WARNING.getColor())
                .setTitle(message.getEmbeds().get(0).getTitle())
                .setDescription(String.format(
                    "%s doesn't have any pets in their %s profile!\n",
                    getUsernameFromPlayer(playerReply), profileReply.getProfile().get("cute_name").getAsString()
                ))
                .build()
            ).queue();
            return false;
        }

        return true;
    }
}
