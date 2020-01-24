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

package com.senither.hypixel.hypixel;

public enum HypixelRank {

    SUPERSTAR("MVP++"),
    MVP_PLUS("MVP+"),
    MVP("MVP"),
    VIP_PLUS("VIP+"),
    VIP("VIP"),
    DEFAULT(null);

    private final String name;
    private boolean aDefault;

    HypixelRank(String name) {
        this.name = name;
    }

    public static HypixelRank getFromType(String type) {
        for (HypixelRank rank : values()) {
            if (rank.name().equalsIgnoreCase(type)) {
                return rank;
            }
        }
        return DEFAULT;
    }

    public static HypixelRank getDefaultRank() {
        return DEFAULT;
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return this == getDefaultRank();
    }
}
