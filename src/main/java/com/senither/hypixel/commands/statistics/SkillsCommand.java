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
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillsCommand extends SkillCommand {

    private final List<Integer> skillLevels = new ArrayList<>();

    public SkillsCommand(SkyblockAssistant app) {
        super(app, "skill");

        skillLevels.addAll(Arrays.asList(
            50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500,
            5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000
        ));

        for (int i = 1; i < 30; i++) {
            skillLevels.add(100000 * i);
        }
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("skills", "skill");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply) {
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
                playerReply.getPlayer().get("displayname").getAsString(), NumberUtil.formatNicelyWithDecimals(
                    (
                        getSkillLevelFromExperience(mining) +
                            getSkillLevelFromExperience(foraging) +
                            getSkillLevelFromExperience(enchanting) +
                            getSkillLevelFromExperience(farming) +
                            getSkillLevelFromExperience(combat) +
                            getSkillLevelFromExperience(fishing) +
                            getSkillLevelFromExperience(alchemy)
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
        return "**LvL:** " + NumberUtil.formatNicelyWithDecimals(getSkillLevelFromExperience(value))
            + "\n**EXP:** " + NumberUtil.formatNicely(value);
    }

    private double getSkillExperience(JsonObject object, String name) {
        try {
            return object.get(name).getAsDouble();
        } catch (Exception e) {
            return 0D;
        }
    }

    private double getSkillLevelFromExperience(double experience) {
        int level = 0;
        for (int toRemove : skillLevels) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return 0;
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
