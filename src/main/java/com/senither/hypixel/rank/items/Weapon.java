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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.senither.hypixel.contracts.rank.ItemRequirement;
import com.senither.hypixel.contracts.rank.WeaponCondition;
import com.senither.hypixel.inventory.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public enum Weapon implements ItemRequirement {

    MIDAS_SWORD(item -> {
        CompoundTag tag = item.getRawCompoundTag().get("tag");
        CompoundTag attributes = tag.get("ExtraAttributes");

        return attributes.contains("winning_bid")
            && ((IntTag) attributes.get("winning_bid")).getValue() >= 50000000;
    }, "Midas' Sword", "midas"),
    REAPER_SCYTHE("Reaper Scythe", "scythe"),
    ASPECT_OF_THE_DRAGONS("Aspect of the Dragons", "aotd"),
    PIGMAN_SWORD("Pigman Sword", "pig sword", "pigman"),
    THICK_SCORPION_FOIL("Thick Scorpion Foil", "foil"),
    POOCH_SWORD("Pooch Sword", "pooch"),
    REAPER_FALCHION("Reaper Falchion", "falchion", "reaper"),
    LEAPING_SWORD("Leaping Sword", "leaping"),
    ASPECT_OF_THE_END("Aspect of the End", "aote"),
    RAIDER_AXE("Raider Axe", "raider"),
    RUNAANS_BOW("Runnan's Bow", "runnan's", "runnans"),
    HURRICANE_BOW("Hurricane Bow", "hurricane"),
    SCORPION_BOW("Scorpion Bow", "scorpion");

    private final String name;
    private final List<String> aliases;
    private final WeaponCondition specialCondition;

    Weapon(String name, String... keywords) {
        this(null, name, keywords);
    }

    Weapon(WeaponCondition condition, String name, String... keywords) {
        this.specialCondition = condition;

        this.name = name;
        this.aliases = new ArrayList<>();
        this.aliases.add(name.toLowerCase());
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    public WeaponCondition getSpecialCondition() {
        return specialCondition;
    }

    public boolean hasSpecialCondition() {
        return specialCondition != null;
    }

    public boolean match(Item item) {
        try {
            return item.getName().endsWith(getName())
                && (!hasSpecialCondition() || getSpecialCondition().matches(item));
        } catch (Exception e) {
            return false;
        }
    }
}
