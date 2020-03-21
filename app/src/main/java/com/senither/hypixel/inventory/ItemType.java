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
import java.util.List;

public enum ItemType {

    WEAPON("sword", "bow"),
    TOOL("pickaxe", "axe", "shovel", "shears", "hoe", "wand"),
    ARMOR("helmet", "chestplate", "leggings", "boots"),
    ACCESSORY("accessory"),
    UNKNOWN();

    private final List<String> types;

    ItemType(String... names) {
        types = Arrays.asList(names);
    }

    public static ItemType fromName(String name) {
        if (name == null) {
            return UNKNOWN;
        }

        for (ItemType type : values()) {
            if (type.getTypes().contains(name.toLowerCase())) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public List<String> getTypes() {
        return types;
    }
}
