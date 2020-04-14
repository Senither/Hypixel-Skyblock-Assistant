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

package com.senither.hypixel.contracts.commands;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.statistics.responses.SkillsResponse;

public abstract class CalculatorCommand extends Command {

    public CalculatorCommand(SkyblockAssistant app) {
        super(app);
    }

    protected final SkillType getSkillTypeFromName(String name, SkillsResponse response) {
        switch (name.toLowerCase()) {
            case "mine":
            case "mining":
                return new SkillType("Mining", response.getMining());

            case "tree":
            case "forage":
            case "foraging":
                return new SkillType("Foraging", response.getForaging());

            case "enchant":
            case "enchanting":
                return new SkillType("Enchanting", response.getEnchanting());

            case "farm":
            case "farming":
                return new SkillType("Farming", response.getFarming());

            case "fight":
            case "combat":
                return new SkillType("Combat", response.getCombat());

            case "fish":
            case "fishing":
                return new SkillType("Fishing", response.getFishing());

            case "alch":
            case "alchemy":
                return new SkillType("Alchemy", response.getAlchemy());

            default:
                return null;
        }
    }

    protected class SkillType {

        String name;
        SkillsResponse.SkillStat stat;

        public SkillType(String name, SkillsResponse.SkillStat stat) {
            this.name = name;
            this.stat = stat;
        }

        public String getName() {
            return name;
        }

        public SkillsResponse.SkillStat getStat() {
            return stat;
        }
    }
}
