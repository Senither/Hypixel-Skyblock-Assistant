/*
 * Copyright (c) 2019.
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

package com.senither.hypixel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.senither.hypixel.inventory.ItemRarity;
import com.senither.hypixel.time.Carbon;

public class Constants {

    public static final String COMMAND_PREFIX = "h!";
    public static final String VERIFY_ROLE = "Verified";
    public static final Carbon STARTED_BOT_AT = Carbon.now();

    public static final ImmutableMultiset<Integer> GENERAL_SKILL_EXPERIENCE = ImmutableMultiset.of(
        50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500,
        5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000, 100000, 200000,
        300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000, 1100000, 1200000,
        1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 1900000, 2000000, 2100000, 2200000,
        2300000, 2400000, 2500000, 2600000, 2750000, 2900000, 3100000, 3400000, 3700000, 4000000
    );

    public static final ImmutableMultiset<Integer> RUNECRAFTING_SKILL_EXPERIENCE = ImmutableMultiset.of(
        50, 100, 125, 160, 200, 250, 315, 400, 500, 625,
        785, 1000, 1250, 1600, 2000, 2465, 3125, 4000, 5000, 6200,
        7800, 9800, 12200, 15300
    );

    public static final ImmutableMultiset<Integer> SLAYER_EXPERIENCE = ImmutableMultiset.of(
        5, 15, 200, 1000, 5000, 20000, 100000, 400000, 1000000
    );

    public static final ImmutableMap<ItemRarity, Integer> PET_OFFSET = ImmutableMap.of(
        ItemRarity.COMMON, 0,
        ItemRarity.UNCOMMON, 6,
        ItemRarity.RARE, 11,
        ItemRarity.EPIC, 16,
        ItemRarity.LEGENDARY, 20
    );

    public static final ImmutableMultiset<Integer> PET_EXPERIENCE = ImmutableMultiset.of(
        100, 110, 120, 130, 145, 160, 175, 190, 210, 230,
        250, 275, 300, 330, 360, 400, 440, 490, 540, 600,
        660, 730, 800, 880, 960, 1050, 1150, 1260, 1380, 1510,
        1650, 1800, 1960, 2130, 2310, 2500, 2700, 2920, 3160, 3420,
        3700, 4000, 4350, 4750, 5200, 5700, 6300, 7000, 7800, 8700,
        9700, 10800, 12000, 13300, 14700, 16200, 17800, 19500, 21300, 23200,
        25200, 27400, 29800, 32400, 35200, 38200, 41400, 44800, 48400, 52200,
        56200, 60400, 64800, 69400, 74200, 79200, 84700, 90700, 97200, 104200,
        111700, 119700, 128200, 137200, 146700, 156700, 167700, 179700, 192700, 206700,
        221700, 237700, 254700, 272700, 291700, 311700, 333700, 357700, 383700, 411700,
        441700, 476700, 516700, 561700, 611700, 666700, 726700, 791700, 861700, 936700,
        1016700, 1101700, 1191700, 1286700, 1386700, 1496700, 1616700, 1746700, 1886700
    );
}
