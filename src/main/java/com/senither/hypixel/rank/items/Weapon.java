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

package com.senither.hypixel.rank.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public enum Weapon {

    REAPER_SCYTHE("Reaper Scythe", "scythe"),
    ASPECT_OF_THE_DRAGONS("Aspect of the Dragons", "aotd"),
    PIGMAN_SWORD("Pigman Sword", "pig sword", "pigman"),
    THICK_SCORPION_FOIL("Thick Scorpion Foil", "foil"),
    POOCH_SWORD("Pooch Sword", "pooch"),
    REAPER_FALCHION("Reaper Falchion", "falchion"),
    LEAPING_SWORD("Leaping Sword", "leaping"),
    ASPECT_OF_THE_END("Aspect of the End", "aote"),
    RAIDER_AXE("Raider Axe", "raider"),
    RUNAANS_BOW("Runnan's Bow", "runnan's", "runnans"),
    HURRICANE_BOW("Hurricane Bow", "hurricane"),
    SCORPION_BOW("Scorpion Bow", "scorpion");

    private final String name;
    private final List<String> aliases;

    Weapon(String name, String... keywords) {
        this.name = name;
        this.aliases = new ArrayList<>();
        this.aliases.add(name().replaceAll("_", " ").toLowerCase());
        this.aliases.addAll(Arrays.asList(keywords));
    }

    public static Weapon getFromName(String name) {
        for (Weapon weapon : values()) {
            if (weapon.getAliases().contains(name.toLowerCase())) {
                return weapon;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
