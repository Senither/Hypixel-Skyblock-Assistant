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

import com.senither.hypixel.contracts.rank.ItemRequirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public enum Armor implements ItemRequirement {

    ELEGANT_TUXEDO(new ArmorSet(
        "Elegant Tuxedo",
        null,
        "Elegant Tuxedo Jacket",
        "Elegant Tuxedo Pants",
        "Elegant Tuxedo Oxfords"
    ), "elegant tuxedo", "etux"),
    FANCY_TUXEDO(new ArmorSet(
        "Fancy Tuxedo",
        null,
        "Fancy Tuxedo Jacket",
        "Fancy Tuxedo Pants",
        "Fancy Tuxedo Oxfords"
    ), "fancy tuxedo", "ftux"),
    SUPERIOR_DRAGON_ARMOR(new ArmorSet(
        "Superior Dragon Armor",
        "Superior Dragon Helmet",
        "Superior Dragon Chestplate",
        "Superior Dragon Leggings",
        "Superior Dragon Boots"
    ), "superior dragon", "superior"),
    STRONG_DRAGON_ARMOR(new ArmorSet(
        "Strong Dragon Armor",
        "Strong Dragon Helmet",
        "Strong Dragon Chestplate",
        "Strong Dragon Leggings",
        "Strong Dragon Boots"
    ), "strong dragon", "strong"),
    UNSTABLE_DRAGON_ARMOR(new ArmorSet(
        "Unstable Dragon Armor",
        "Unstable Dragon Helmet",
        "Unstable Dragon Chestplate",
        "Unstable Dragon Leggings",
        "Unstable Dragon Boots"
    ), "unstable dragon", "unstable"),
    WISE_DRAGON_ARMOR(new ArmorSet(
        "Wise Dragon Armor",
        "Wise Dragon Helmet",
        "Wise Dragon Chestplate",
        "Wise Dragon Leggings",
        "Wise Dragon Boots"
    ), "wise dragon", "wise"),
    YOUNG_DRAGON_ARMOR(new ArmorSet(
        "Young Dragon Armor",
        "Young Dragon Helmet",
        "Young Dragon Chestplate",
        "Young Dragon Leggings",
        "Young Dragon Boots"
    ), "young dragon", "young"),
    PROTECTOR_DRAGON_ARMOR(new ArmorSet(
        "Protector Dragon Armor",
        "Protector Dragon Helmet",
        "Protector Dragon Chestplate",
        "Protector Dragon Leggings",
        "Protector Dragon Boots"
    ), "protector dragon", "protector"),
    OLD_DRAGON_ARMOR(new ArmorSet(
        "Old Dragon Armor",
        "Old Dragon Helmet",
        "Old Dragon Chestplate",
        "Old Dragon Leggings",
        "Old Dragon Boots"
    ), "old dragon", "old", "boomer"),
    REVENANT_ARMOR(new ArmorSet(
        "Revenant Horror Armor",
        null,
        "Revenant Chestplate",
        "Revenant Leggings",
        "Revenant Boots"
    ), "revenant", "rev"),
    TARANTULA_ARMOR(new ArmorSet(
        "Tarantula Armor",
        "Tarantula Helmet",
        "Tarantula Chestplate",
        "Tarantula Leggings",
        "Tarantula Boots"
    ), "tarantula", "tara"),
    MASTIFF_ARMOR(new ArmorSet(
        "Mastiff Armor",
        "Mastiff Crown",
        "Mastiff Chestplate",
        "Mastiff Leggings",
        "Mastiff Boots"
    ), "mastiff"),
    BAT_PERSON_ARMOR(new ArmorSet(
        "Bat Person Armor",
        "Bat Person Helmet",
        "Bat Person Chestplate",
        "Bat Person Leggings",
        "Bat Person Boots"
    ), "bat person", "bat"),
    DIVERS_ARMOR(new ArmorSet(
        "Diver's Armor",
        "Diver's Mask",
        "Diver's Shirt",
        "Diver's Trunks",
        "Diver's Boots"
    ), "diver armor", "diver");

    private final ArmorSet armorSet;
    private final List<String> aliases;

    Armor(ArmorSet armorSet, String... keywords) {
        this.armorSet = armorSet;
        this.aliases = new ArrayList<>();
        this.aliases.add(name().replaceAll("_", " ").toLowerCase());
        this.aliases.addAll(Arrays.asList(keywords));
    }

    public static Armor getFromName(String name) {
        for (Armor armor : values()) {
            if (armor.getName().equalsIgnoreCase(name) || armor.getAliases().contains(name.toLowerCase())) {
                return armor;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return getArmorSet().getName();
    }

    public ArmorSet getArmorSet() {
        return armorSet;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }
}
