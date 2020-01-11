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

    private final List<Integer> generalSkillLevels = new ArrayList<>();
    private final List<Integer> runecraftinSkillLevels = new ArrayList<>();

    public SkillsCommand(SkyblockAssistant app) {
        super(app, "skill");

        generalSkillLevels.addAll(Arrays.asList(
            50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500,
            5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000
        ));

        runecraftinSkillLevels.addAll(Arrays.asList(
            50, 100, 125, 160, 200, 250, 315, 400, 500, 625,
            785, 1000, 1250, 1600, 2000, 2465, 3125, 4000, 5000, 6200,
            7800, 9800, 12200, 15300
        ));

        int index = 1;
        while (generalSkillLevels.size() < 50) {
            generalSkillLevels.add(100000 * index++);
        }
    }

    @Override
    public String getName() {
        return "Skills Statistics";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets a players average skill level, as-well-as all their skill experience and levels.",
            "\n\n**Note:** This command will only work for users who have enabled their skill API."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username>` - Gets skill stats for the given username",
            "`:command <mention>` - Gets skill stats for the mentioned Discord user"
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
            sendAPIIsDisabledMessage(message, profileReply, playerReply.getPlayer().get("displayname").getAsString());
            return;
        }

        message.editMessage(new EmbedBuilder()
            .setTitle(playerReply.getPlayer().get("displayname").getAsString() + "'s Skills")
            .setDescription(String.format("**%s** has an average skill level of **%s**",
                playerReply.getPlayer().get("displayname").getAsString(), NumberUtil.formatNicelyWithDecimals(
                    (
                        getSkillLevelFromExperience(mining, false) +
                            getSkillLevelFromExperience(foraging, false) +
                            getSkillLevelFromExperience(enchanting, false) +
                            getSkillLevelFromExperience(farming, false) +
                            getSkillLevelFromExperience(combat, false) +
                            getSkillLevelFromExperience(fishing, false) +
                            getSkillLevelFromExperience(alchemy, false)
                    ) / 7D)
            ))
            .setColor(MessageType.SUCCESS.getColor())
            .addField("Mining", formatStatTextValue(mining, false), true)
            .addField("Foraging", formatStatTextValue(foraging, false), true)
            .addField("Enchanting", formatStatTextValue(enchanting, false), true)
            .addField("Farming", formatStatTextValue(farming, false), true)
            .addField("Combat", formatStatTextValue(combat, false), true)
            .addField("Fishing", formatStatTextValue(fishing, false), true)
            .addField("Alchemy", formatStatTextValue(alchemy, false), true)
            .addField("Carpentry", formatStatTextValue(carpentry, false), true)
            .addField("Runecrafting", formatStatTextValue(runecrafting, true), true)
            .setFooter(String.format(
                "Note > Carpentry and Runecrafting are cosmetic skills, and are therefor not included in the average skill calculation. | Profile: %s",
                profileReply.getProfile().get("cute_name").getAsString()
            ))
            .build()
        ).queue();
    }

    private String formatStatTextValue(double value, boolean isRunecrafting) {
        return "**LvL:** " + NumberUtil.formatNicelyWithDecimals(getSkillLevelFromExperience(value, isRunecrafting))
            + "\n**EXP:** " + NumberUtil.formatNicely(value);
    }

    private double getSkillExperience(JsonObject object, String name) {
        try {
            return object.get(name).getAsDouble();
        } catch (Exception e) {
            return 0D;
        }
    }

    private double getSkillLevelFromExperience(double experience, boolean isRunecrafting) {
        int level = 0;
        for (int toRemove : isRunecrafting ? runecraftinSkillLevels : generalSkillLevels) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return level;
    }

    private void sendAPIIsDisabledMessage(Message message, SkyBlockProfileReply profileReply, String username) {
        message.editMessage(new EmbedBuilder()
            .setColor(MessageType.WARNING.getColor())
            .setTitle("Failed to load profile!")
            .setDescription(String.format(
                "%s has their skills API disabled for their %s profile!\nYou can ask them nicely to enable it.",
                username, profileReply.getProfile().get("cute_name").getAsString()
            ))
            .build()
        ).queue();
    }
}
