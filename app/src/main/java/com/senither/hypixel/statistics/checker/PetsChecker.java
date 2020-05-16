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

package com.senither.hypixel.statistics.checker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.senither.hypixel.contracts.statistics.Checker;
import com.senither.hypixel.statistics.responses.PetsResponse;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import javax.annotation.Nullable;

public class PetsChecker extends Checker<PetsResponse> {

    @Override
    public PetsResponse checkUser(@Nullable PlayerReply playerReply, SkyBlockProfileReply profileReply, JsonObject member) {
        if (!member.has("pets")) {
            return new PetsResponse(false, false);
        }

        if (member.getAsJsonArray("pets").size() == 0) {
            return new PetsResponse(true, false);
        }

        PetsResponse response = new PetsResponse(true, true);

        for (JsonElement element : member.getAsJsonArray("pets")) {
            JsonObject pet = element.getAsJsonObject();

            JsonElement heldItem = pet.get("heldItem");

            response.addPet(
                pet.get("type").getAsString(),
                pet.get("tier").getAsString(),
                pet.get("exp").getAsLong(),
                heldItem == null || heldItem.isJsonNull()
                    ? null : heldItem.getAsString(),
                pet.get("active").getAsBoolean()
            );
        }

        return response;
    }
}
