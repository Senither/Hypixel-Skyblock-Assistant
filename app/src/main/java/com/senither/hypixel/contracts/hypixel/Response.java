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

package com.senither.hypixel.contracts.hypixel;

import com.senither.hypixel.time.Carbon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public abstract class Response {

    public static final SimpleDateFormat ISO_8601_DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);

    protected int status;

    protected static Carbon timestampToCarbonInstance(String timestamp) {
        try {
            Date date = Response.ISO_8601_DateFormat.parse(timestamp
                .replace("Z", "0")
                .replace(".", "+")
            );

            return Carbon.now().setTimestamp(date.getTime() / 1000);
        } catch (ParseException e) {
            return null;
        }
    }

    public boolean isSuccess() {
        return getStatus() == 200;
    }

    public int getStatus() {
        return status;
    }
}
