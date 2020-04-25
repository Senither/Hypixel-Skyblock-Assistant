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

package com.senither.hypixel.inventory;

import java.util.Arrays;
import java.util.HashSet;

public enum ItemRarity {

    SPECIAL("Special", "spec", "s"),
    LEGENDARY("Legendary", "legend", "leg", "l"),
    EPIC("Epic", "e"),
    RARE("Rare", "r"),
    UNCOMMON("Uncommon", "uncom", "u"),
    COMMON("Common", "com", "c"),
    UNKNOWN(true, "Unknown");

    private final boolean isDefault;
    private final String name;
    private final HashSet<String> aliases;

    ItemRarity(String name, String... aliases) {
        this(false, name, aliases);
    }

    ItemRarity(boolean isDefault, String name, String... aliases) {
        this.isDefault = isDefault;
        this.name = name;

        this.aliases = new HashSet<>(Arrays.asList(aliases));
        this.aliases.add(name);
    }

    public static ItemRarity fromName(String name) {
        if (name == null) {
            return UNKNOWN;
        }

        for (ItemRarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return UNKNOWN;
    }

    public static ItemRarity fromAlias(String name) {
        if (name == null) {
            return UNKNOWN;
        }

        for (ItemRarity rarity : values()) {
            for (String alias : rarity.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return rarity;
                }
            }
        }
        return UNKNOWN;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getName() {
        return name;
    }

    public HashSet<String> getAliases() {
        return aliases;
    }
}
