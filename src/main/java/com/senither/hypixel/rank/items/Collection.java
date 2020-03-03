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
    WHEAT("WHEAT", 25000),
    CARROT("CARROT_ITEM", 100000),
    POTATO("POTATO_ITEM", 100000),
    PUMPKIN("PUMPKIN", 100000),
    MELON("MELON", 250000),
    SEEDS("SEEDS", 5000),
    MUSHROOM("MUSHROOM_COLLECTION", 50000),
    COCOA_BEANS("INK_SACK:3", 100000),
    CACTUS("CACTUS", 50000),
    SUGAR_CANE("SUGAR_CANE", 50000),
    FEATHER("FEATHER", 50000),
    LEATHER("LEATHER", 100000),
    PORK("PORK", 50000),
    CHICKEN("RAW_CHICKEN", 50000),
    MUTTON("MUTTON", 50000),
    RABBIT("RABBIT", 50000),
    NETHER_WART("NETHER_STALK", 50000),

    // Mining Collection
    COBBLESTONE("COBBLESTONE", 70000),
    COAL("COAL", 50000),
    IRON("IRON_INGOT", 50000),
    GOLD("GOLD_INGOT", 50000),
    DIAMOND("DIAMOND", 50000),
    LAPIS_LAZULI("INK_SACK:4", 150000),
    EMERALD("EMERALD", 100000),
    REDSTONE("REDSTONE", 800000),
    QUARTZ("QUARTZ", 50000),
    OBSIDIAN("OBSIDIAN", 50000),
    GLOWSTONE("GLOWSTONE_DUST", 50000),
    GRAVEL("GRAVEL", 50000),
    ICE("ICE", 250000),
    NETHERRACK("NETHERRACK", 500),
    SAND("SAND", 5000),
    END_STONE("ENDER_STONE", 50000),

    // Combat Collection
    ROTTEN_FLESH("ROTTEN_FLESH", 50000),
    BONE("BONE", 50000),
    STRING("STRING", 50000),
    SPIDER_EYE("SPIDER_EYE", 50000),
    GUNPOWDER("SULPHUR", 50000),
    ENDER_PEARL("ENDER_PEARL", 50000),
    GHAST_TEAR("GHAST_TEAR", 50000),
    SLIME_BALL("SLIME_BALL", 50000),
    BLAZE_ROD("BLAZE_ROD", 50000),
    MAGMA_CREAM("MAGMA_CREAM", 50000),

    // Wood Collection
    OAK("LOG", 30000),
    SPRUCE("LOG:1", 50000),
    BIRCH("LOG:2", 10000),
    DARK_OAK("LOG_2:1", 50000),
    ACACIA("LOG_2", 25000),
    JUNGLE("LOG:3", 50000),

    RAW_FISH("RAW_FISH", 30000),
    RAW_SALMON("RAW_FISH:1", 10000),
    CLONFISH("RAW_FISH:2", 50),
    PUFFERFISH("RAW_FISH:3", 800),
    PRISMARINE_SHARD("PRISMARINE_SHARD", 200),
    PRISMARINE_CRYSTALS("PRISMARINE_CRYSTALS", 800),
    CLAY("CLAY_BALL", 2500),
    LILY_PAD("WATER_LILY", 10000),
    INK_SACK("INK_SACK", 4000),
    SPONGE("SPONGE", 4000);

    private final String key;
    private final long maxLevelExperience;

    Collection(String key, long maxLevelExperience) {
        this.key = key;
        this.maxLevelExperience = maxLevelExperience;
    }

    public String getKey() {
        return key;
    }

    public long getMaxLevelExperience() {
        return maxLevelExperience;
    }
}
