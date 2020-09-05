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

package com.senither.hypixel.commands.calculators;

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.inventory.ItemRarity;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PetsCalculatorCommand extends Command {

    public PetsCalculatorCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Pets Calculator";
    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList(
            "Calculates the needed XP between two levels for any rarity of pets."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <rarity> <to> <from>` - Calculates the required XP using the given levels and rarity."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command rare 3 45` - Calculates the XP needed to go from level 3 to 45 for rare pets.",
            "`:command legendary 1 50` - Calculates the XP needed to go from level 1 to 50 for legendary pets."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("calcpet", "calcpets");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(), String.join("\n", Arrays.asList(
                "You must include the rarity of the pet you wish to do the calculation for, the rarity can be one of the following:",
                "`Legendary`, `Epic`, `Rare`, `Uncommon`, or `Common`"
            ))).setTitle("Missing argument").queue();
            return;
        }

        ItemRarity rarity = ItemRarity.fromAlias(args[0]);
        if (rarity == ItemRarity.UNKNOWN || rarity == ItemRarity.SPECIAL) {
            MessageFactory.makeError(event.getMessage(), String.join("\n", Arrays.asList(
                "Invalid pet rarity given, the rarity must be one of the following:",
                "`Legendary`, `Epic`, `Rare`, `Uncommon`, or `Common`"
            ))).setTitle("Invalid argument").queue();
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "You must specify the minimum level you want the calculate the xp different for."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (args.length == 2) {
            MessageFactory.makeError(event.getMessage(),
                "You must specify the maximum level so the xp difference can be calculated."
            ).setTitle("Missing argument").queue();
            return;
        }

        int min = NumberUtil.getBetween(NumberUtil.parseInt(args[1], 0), 1, 100);
        int max = NumberUtil.getBetween(NumberUtil.parseInt(args[2], 0), 1, 100);

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        int xp = 0;
        Integer offset = Constants.PET_OFFSET.get(rarity);
        for (int i = offset + min - 1; i < offset + max - 1; i++) {
            xp += Constants.PET_EXPERIENCE.asList().get(i);
        }

        MessageFactory.makeInfo(event.getMessage(), "You need **:xp** XP to level a :rarity pet from level **:first** to **:second**!")
            .set("rarity", rarity.getName())
            .set("first", min)
            .set("second", max)
            .set("xp", NumberUtil.formatNicely(xp))
            .queue();
    }
}
