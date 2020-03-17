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

package com.senither.hypixel.contracts.statistics;

import com.google.gson.JsonObject;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import javax.annotation.Nullable;

public abstract class Checker<T extends StatisticsResponse> {

    public abstract T checkUser(@Nullable PlayerReply playerReply, SkyBlockProfileReply profileReply, JsonObject member);

    protected double getDoubleFromObject(JsonObject object, String name) {
        try {
            return object.get(name).getAsDouble();
        } catch (Exception e) {
            return 0D;
        }
    }
}
