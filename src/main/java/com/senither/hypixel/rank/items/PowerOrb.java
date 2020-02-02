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

public enum PowerOrb {

    RADIANT_ORB(1, "Radiant Power Orb"),
    MANA_FLUX(2, "Mana Flux Power Orb"),
    OVERFLUX(3, "Overflux Power Orb");

    private final int id;
    private final String name;

    PowerOrb(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static PowerOrb fromId(int id) {
        for (PowerOrb orb : values()) {
            if (orb.getId() == id) {
                return orb;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
