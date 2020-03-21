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

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class NumberUtil {

    private static final Pattern numberPattern = Pattern.compile("[-+]?\\d*\\.?\\d+");
    private static final DecimalFormat niceFormat = new DecimalFormat("#,##0");
    private static final DecimalFormat niceFormatWithDecimal = new DecimalFormat("#,###.##");

    public static int parseInt(String number, int fallback) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static int getBetween(int number, int min, int max) {
        return Math.min(max, Math.max(min, number));
    }

    public static String formatNicely(int value) {
        return niceFormat.format(value);
    }

    public static String formatNicely(double value) {
        return niceFormat.format(value);
    }

    public static String formatNicely(long value) {
        return niceFormat.format(value);
    }

    public static String formatNicelyWithDecimals(double value) {
        return niceFormatWithDecimal.format(value);
    }

    public static boolean isNumeric(@Nonnull String string) {
        return numberPattern.matcher(string).matches();
    }
}
