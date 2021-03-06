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

package com.senither.hypixel.statistics;

import com.senither.hypixel.statistics.checker.DungeonChecker;
import com.senither.hypixel.statistics.checker.PetsChecker;
import com.senither.hypixel.statistics.checker.SkillsChecker;
import com.senither.hypixel.statistics.checker.SlayerChecker;

@SuppressWarnings("unused")
public class StatisticsChecker {

    public final static DungeonChecker DUNGEON = new DungeonChecker();
    public final static SkillsChecker SKILLS = new SkillsChecker();
    public final static SlayerChecker SLAYER = new SlayerChecker();
    public final static PetsChecker PETS = new PetsChecker();
}
