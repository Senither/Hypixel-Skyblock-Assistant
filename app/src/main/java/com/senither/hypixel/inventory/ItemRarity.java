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

public enum ItemRarity {

    SPECIAL("Special"),
    LEGENDARY("Legendary"),
    EPIC("Epic"),
    RARE("Rare"),
    UNCOMMON("Uncommon"),
    COMMON("Common"),
    UNKNOWN(true, "Unknown");

    private final boolean isDefault;
    private final String name;

    ItemRarity(String name) {
        this(false, name);
    }

    ItemRarity(boolean isDefault, String name) {
        this.isDefault = isDefault;
        this.name = name;
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

    public boolean isDefault() {
        return isDefault;
    }

    public String getName() {
        return name;
    }
}
