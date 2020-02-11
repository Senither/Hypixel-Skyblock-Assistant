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

package com.senither.hypixel.rank;

import com.senither.hypixel.contracts.rank.RankCommandHandler;
import com.senither.hypixel.contracts.rank.RankRequirementChecker;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.rank.checkers.*;
import com.senither.hypixel.rank.handler.CustomObjectValueHandler;
import com.senither.hypixel.rank.handler.DoubleObjectValueHandler;
import com.senither.hypixel.rank.handler.IntegerValueHandler;
import com.senither.hypixel.rank.handler.ItemValueHandler;
import com.senither.hypixel.rank.items.Armor;
import com.senither.hypixel.rank.items.PowerOrb;
import com.senither.hypixel.rank.items.Weapon;
import com.senither.hypixel.utils.NumberUtil;

public enum RankRequirementType {

    TALISMANS("Talismans", new TalismansChecker(), new DoubleObjectValueHandler(
        GuildController.GuildEntry.RankRequirement::setTalismansLegendary,
        GuildController.GuildEntry.RankRequirement::setTalismansEpic,
        "The new Talismans requirement for **:rank** have successfully been set to **:first** legendaries, and **:second** epics."
    )),
    AVERAGE_SKILLS("Average Skills", new AverageSkillsChecker(), new IntegerValueHandler(
        GuildController.GuildEntry.RankRequirement::setAverageSkills
    )),
    SLAYER("Slayer XP", new SlayerChecker(), new IntegerValueHandler(
        GuildController.GuildEntry.RankRequirement::setSlayerExperience
    )),
    FAIRY_SOULS("Fairy Souls", new FairySoulsChecker(), new IntegerValueHandler(
        GuildController.GuildEntry.RankRequirement::setFairySouls
    )),
    POWER_ORBS("Power Orbs", new PowerOrbsChecker(), new CustomObjectValueHandler<>(
        value -> PowerOrb.fromId(NumberUtil.parseInt(value.toString(), -1)),
        GuildController.GuildEntry.RankRequirement::setPowerOrb
    )),
    ARMOR("Armor", new ArmorChecker(), new ItemValueHandler(
        GuildController.GuildEntry.RankRequirement::setArmorPoints,
        GuildController.GuildEntry.RankRequirement::getArmorItems,
        Armor.values()
    )),
    WEAPONS("Weapon", new WeaponsChecker(), new ItemValueHandler(
        GuildController.GuildEntry.RankRequirement::setWeaponPoints,
        GuildController.GuildEntry.RankRequirement::getWeaponItems,
        Weapon.values())
    );

    private final String name;
    private final RankRequirementChecker checker;
    private final RankCommandHandler handler;

    RankRequirementType(String name, RankRequirementChecker checker, RankCommandHandler handler) {
        this.name = name;
        this.checker = checker;
        this.handler = handler;

        this.handler.setRankType(this);
    }

    public String getName() {
        return name;
    }

    public RankRequirementChecker getChecker() {
        return checker;
    }

    public RankCommandHandler getHandler() {
        return handler;
    }
}
