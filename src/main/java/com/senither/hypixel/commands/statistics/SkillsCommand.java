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
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SkillsCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(SkillsCommand.class);
    private static final DecimalFormat niceFormatWithDecimal = new DecimalFormat("#,###.##");

    public SkillsCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("skills", "skill");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setColor(MessageType.ERROR.getColor())
                .setTitle("Missing username")
                .setDescription(String.join("\n", Arrays.asList(
                    "You must include the username of the user you want to see their skills from.",
                    "",
                    "Try again using `h!skills <username>`"
                )))
                .build()
            ).queue();
            return;
        }

        if (!app.getHypixel().isValidMinecraftUsername(args[0])) {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setDescription("Invalid Minecraft username given! You must provide a valid to see the users skills.")
                .setColor(MessageType.ERROR.getColor())
                .build()
            ).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle(args[0] + "'s Skills")
            .setDescription("Loading Skyblock profile data for " + args[0] + "!")
            .setColor(MessageType.INFO.getColor());

        event.getChannel().sendMessage(embedBuilder.build()).queue(message -> {
            app.getHypixel().getSelectedSkyBlockProfileFromUsername(args[0]).whenCompleteAsync((playerReply, throwable) -> {
                if (throwable == null) {
                    try {
                        handleSkyblockProfile(message, playerReply, app.getHypixel().getPlayerByName(args[0]).get(10, TimeUnit.SECONDS));
                        return;
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        throwable = e;
                    }
                }

                log.error("Failed to get player data by name, error: {}", throwable.getMessage(), throwable);

                message.editMessage(embedBuilder
                    .setDescription("Something went wrong: " + throwable.getMessage())
                    .setColor(MessageType.ERROR.getColor())
                    .build()
                ).queue();
            });
        });
    }

    private void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
        JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(playerReply.getPlayer().get("uuid").getAsString());

        double mining = getSkillExperience(member, "experience_skill_mining");
        double foraging = getSkillExperience(member, "experience_skill_foraging");
        double enchanting = getSkillExperience(member, "experience_skill_enchanting");
        double farming = getSkillExperience(member, "experience_skill_farming");
        double combat = getSkillExperience(member, "experience_skill_combat");
        double fishing = getSkillExperience(member, "experience_skill_fishing");
        double alchemy = getSkillExperience(member, "experience_skill_alchemy");
        double carpentry = getSkillExperience(member, "experience_skill_carpentry");
        double runecrafting = getSkillExperience(member, "experience_skill_runecrafting");

        if (mining + foraging + enchanting + farming + combat + fishing + alchemy == 0) {
            sendAPIIsDisabledMessage(message, new EmbedBuilder(), playerReply.getPlayer().get("displayname").getAsString());
            return;
        }

        message.editMessage(new EmbedBuilder()
            .setTitle(playerReply.getPlayer().get("displayname").getAsString() + "'s Skills")
            .setDescription(String.format("**%s** has an average skill level of **%s**",
                playerReply.getPlayer().get("displayname").getAsString(), niceFormatWithDecimal.format(
                    (
                        app.getHypixel().getSkillLevelFromExperience(mining) +
                            app.getHypixel().getSkillLevelFromExperience(foraging) +
                            app.getHypixel().getSkillLevelFromExperience(enchanting) +
                            app.getHypixel().getSkillLevelFromExperience(farming) +
                            app.getHypixel().getSkillLevelFromExperience(combat) +
                            app.getHypixel().getSkillLevelFromExperience(fishing) +
                            app.getHypixel().getSkillLevelFromExperience(alchemy)
                    ) / 7D)
            ))
            .setColor(MessageType.SUCCESS.getColor())
            .addField("Mining", formatStatTextValue(mining), true)
            .addField("Foraging", formatStatTextValue(foraging), true)
            .addField("Enchanting", formatStatTextValue(enchanting), true)
            .addField("Farming", formatStatTextValue(farming), true)
            .addField("Combat", formatStatTextValue(combat), true)
            .addField("Fishing", formatStatTextValue(fishing), true)
            .addField("Alchemy", formatStatTextValue(alchemy), true)
            .addField("Carpentry", formatStatTextValue(carpentry), true)
            .addField("Runecrafting", formatStatTextValue(runecrafting), true)
            .setFooter("Note > Carpentry and Runecrafting are cosmetic skills, and are therefor not included in the average skill calculation.")
            .build()
        ).queue();
    }

    private String formatStatTextValue(double value) {
        return "**LvL:** " + niceFormatWithDecimal.format(app.getHypixel().getSkillLevelFromExperience(value))
            + "\n**EXP:** " + niceFormatWithDecimal.format(value);
    }

    private double getSkillExperience(JsonObject object, String name) {
        try {
            return object.get(name).getAsDouble();
        } catch (Exception e) {
            return 0D;
        }
    }

    private void sendAPIIsDisabledMessage(Message message, EmbedBuilder embedBuilder, String username) {
        message.editMessage(embedBuilder
            .setColor(MessageType.WARNING.getColor())
            .setTitle("Failed to load profile!")
            .setDescription(username + " doesn't appear to have their skill API option enabled!\nYou can ask them nicely to enable it.")
            .build()
        ).queue();
    }
}
