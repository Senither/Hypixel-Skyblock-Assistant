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

import com.google.common.collect.ImmutableMultiset;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.statistics.HasLevel;
import com.senither.hypixel.statistics.responses.DungeonResponse;
import com.senither.hypixel.statistics.responses.SkillsResponse;

public abstract class CalculatorCommand extends Command {

    public CalculatorCommand(SkyblockAssistant app) {
        super(app);
    }

    protected final SkillType getSkillTypeFromName(String name, SkillsResponse skillsResponse, DungeonResponse dungeonResponse) {
        switch (name.toLowerCase()) {
            case "mine":
            case "mining":
                return new SkillType("Mining", skillsResponse.getMining(), SkillCalculationType.GENERAL);

            case "tree":
            case "forage":
            case "foraging":
                return new SkillType("Foraging", skillsResponse.getForaging(), SkillCalculationType.GENERAL);

            case "enchant":
            case "enchanting":
                return new SkillType("Enchanting", skillsResponse.getEnchanting(), SkillCalculationType.GENERAL);

            case "farm":
            case "farming":
                return new SkillType("Farming", skillsResponse.getFarming(), SkillCalculationType.GENERAL);

            case "fight":
            case "combat":
                return new SkillType("Combat", skillsResponse.getCombat(), SkillCalculationType.GENERAL);

            case "fish":
            case "fishing":
                return new SkillType("Fishing", skillsResponse.getFishing(), SkillCalculationType.GENERAL);

            case "alch":
            case "alchemy":
                return new SkillType("Alchemy", skillsResponse.getAlchemy(), SkillCalculationType.GENERAL);

            case "pet":
            case "pets":
            case "tame":
            case "taming":
                return new SkillType("Taming", skillsResponse.getTaming(), SkillCalculationType.GENERAL);

            case "rune":
            case "runecraft":
            case "runecrafting":
                return new SkillType("Runecrafting", skillsResponse.getRunecrafting(), SkillCalculationType.RUNECRAFTING);

            case "ca":
            case "cata":
            case "catacomb":
            case "catacombs":
                return new SkillType("Catacomb", dungeonResponse.getDungeonFromType(DungeonResponse.DungeonType.CATACOMBS), SkillCalculationType.DUNGEON);

            case "heal":
            case "healer":
                return new SkillType("Healer", dungeonResponse.getClassFromType(DungeonResponse.DungeonClassType.HEALER), SkillCalculationType.DUNGEON);

            case "mage":
            case "mages":
                return new SkillType("Mage", dungeonResponse.getClassFromType(DungeonResponse.DungeonClassType.MAGE), SkillCalculationType.DUNGEON);

            case "berserk":
            case "warrior":
                return new SkillType("Berserk", dungeonResponse.getClassFromType(DungeonResponse.DungeonClassType.BERSERK), SkillCalculationType.DUNGEON);

            case "bow":
            case "archer":
                return new SkillType("Berserk", dungeonResponse.getClassFromType(DungeonResponse.DungeonClassType.ARCHER), SkillCalculationType.DUNGEON);

            case "tank":
                return new SkillType("Berserk", dungeonResponse.getClassFromType(DungeonResponse.DungeonClassType.TANK), SkillCalculationType.DUNGEON);

            default:
                return null;
        }
    }

    protected enum SkillCalculationType {

        GENERAL, RUNECRAFTING, DUNGEON
    }

    protected class SkillType {

        private final String name;
        private final HasLevel stat;
        private final SkillCalculationType type;

        public SkillType(String name, HasLevel stat, SkillCalculationType type) {
            this.name = name;
            this.stat = stat;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public HasLevel getStat() {
            return stat;
        }

        public SkillCalculationType getType() {
            return type;
        }

        public ImmutableMultiset<Integer> getExperienceList() {
            switch (type) {
                case GENERAL:
                    return Constants.GENERAL_SKILL_EXPERIENCE;

                case RUNECRAFTING:
                    return Constants.RUNECRAFTING_SKILL_EXPERIENCE;

                case DUNGEON:
                    return Constants.DUNGEON_EXPERIENCE;

                default:
                    throw new RuntimeException("No valid skill experience calculator could be found for type '" + type + "'");
            }
        }
    }
}
