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

public class ArmorSet {

    private final String helmet;
    private final String chestplate;
    private final String leggings;
    private final String boots;

    private final int setPieces;

    ArmorSet(String helmet, String chestplate, String leggings, String boots) {
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;

        int pieces = helmet == null ? 0 : 1;
        pieces += chestplate == null ? 0 : 1;
        pieces += leggings == null ? 0 : 1;
        pieces += boots == null ? 0 : 1;

        this.setPieces = pieces;
    }

    public String getHelmet() {
        return helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public String getBoots() {
        return boots;
    }

    public int getSetPieces() {
        return setPieces;
    }

    public boolean isPartOfSet(String itemName) {
        return (helmet != null && itemName.endsWith(helmet))
            || (chestplate != null && itemName.endsWith(chestplate))
            || (leggings != null && itemName.endsWith(leggings))
            || (boots != null && itemName.endsWith(boots));
    }
}
