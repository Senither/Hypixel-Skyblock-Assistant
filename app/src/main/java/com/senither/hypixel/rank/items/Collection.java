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

@SuppressWarnings("SpellCheckingInspection")
public enum Collection {

    // Farming Collection
    WHEAT("WHEAT", 9),
    CARROT("CARROT_ITEM", 9),
    POTATO("POTATO_ITEM", 9),
    PUMPKIN("PUMPKIN", 9),
    MELON("MELON", 9),
    SEEDS("SEEDS", 6),
    MUSHROOM("MUSHROOM_COLLECTION", 9),
    COCOA_BEANS("INK_SACK:3", 9),
    CACTUS("CACTUS", 9),
    SUGAR_CANE("SUGAR_CANE", 9),
    FEATHER("FEATHER", 9),
    LEATHER("LEATHER", 10),
    PORK("PORK", 9),
    CHICKEN("RAW_CHICKEN", 9),
    MUTTON("MUTTON", 9),
    RABBIT("RABBIT", 9),
    NETHER_WART("NETHER_STALK", 9),

    // Mining Collection
    COBBLESTONE("COBBLESTONE", 9),
    COAL("COAL", 9),
    IRON("IRON_INGOT", 9),
    GOLD("GOLD_INGOT", 9),
    DIAMOND("DIAMOND", 9),
    LAPIS_LAZULI("INK_SACK:4", 9),
    EMERALD("EMERALD", 9),
    REDSTONE("REDSTONE", 13),
    QUARTZ("QUARTZ", 9),
    OBSIDIAN("OBSIDIAN", 9),
    GLOWSTONE("GLOWSTONE_DUST", 9),
    GRAVEL("GRAVEL", 9),
    ICE("ICE", 10),
    NETHERRACK("NETHERRACK", 3),
    SAND("SAND", 7),
    END_STONE("ENDER_STONE", 9),

    // Combat Collection
    ROTTEN_FLESH("ROTTEN_FLESH", 9),
    BONE("BONE", 9),
    STRING("STRING", 9),
    SPIDER_EYE("SPIDER_EYE", 9),
    GUNPOWDER("SULPHUR", 9),
    ENDER_PEARL("ENDER_PEARL", 9),
    GHAST_TEAR("GHAST_TEAR", 9),
    SLIME_BALL("SLIME_BALL", 9),
    BLAZE_ROD("BLAZE_ROD", 9),
    MAGMA_CREAM("MAGMA_CREAM", 9),

    // Wood Collection
    OAK("LOG", 9),
    SPRUCE("LOG:1", 9),
    BIRCH("LOG:2", 9),
    DARK_OAK("LOG_2:1", 9),
    ACACIA("LOG_2", 9),
    JUNGLE("LOG:3", 9),

    // Fishing Collection
    RAW_FISH("RAW_FISH", 9),
    RAW_SALMON("RAW_FISH:1", 9),
    CLONFISH("RAW_FISH:2", 3),
    PUFFERFISH("RAW_FISH:3", 6),
    PRISMARINE_SHARD("PRISMARINE_SHARD", 5),
    PRISMARINE_CRYSTALS("PRISMARINE_CRYSTALS", 7),
    CLAY("CLAY_BALL", 5),
    LILY_PAD("WATER_LILY", 9),
    INK_SACK("INK_SACK", 9),
    SPONGE("SPONGE", 9);

    private final String key;
    private final int maxLevel;

    Collection(String key, int maxLevel) {
        this.key = key;
        this.maxLevel = maxLevel;
    }

    public String getKey() {
        return key;
    }

    public long getMaxLevel() {
        return maxLevel;
    }
}
