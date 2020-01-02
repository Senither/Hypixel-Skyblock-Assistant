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

package com.senither.hypixel.utils;

import java.text.DecimalFormat;

public class NumberUtil {

    private static final DecimalFormat niceFormat = new DecimalFormat("#,##0");
    private static final DecimalFormat niceFormatWithDecimal = new DecimalFormat("#,###.##");

    public static String formatNicely(int value) {
        return niceFormat.format(value);
    }

    public static String formatNicely(double value) {
        return niceFormat.format(value);
    }

    public static String formatNicelyWithDecimals(double value) {
        return niceFormatWithDecimal.format(value);
    }
}
