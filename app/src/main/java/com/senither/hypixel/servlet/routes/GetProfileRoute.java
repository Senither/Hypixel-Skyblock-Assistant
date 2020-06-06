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

package com.senither.hypixel.servlet.routes;

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.servlet.SparkRoute;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.statistics.StatisticsChecker;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import spark.Request;
import spark.Response;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GetProfileRoute extends SparkRoute {

    public GetProfileRoute(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!isAuthorized(request)) {
            return generateUnauthornizedResponse(response);
        }

        String username = request.params("username");
        if (!app.getHypixel().isValidMinecraftUsername(username)) {
            try {
                UUID uuid = UUID.fromString(username);

                username = app.getHypixel().getUsernameFromUuid(uuid);

                if (username == null) {
                    throw new FriendlyException("Invalid Minecraft username UUID provided");
                }
            } catch (IllegalArgumentException e) {
                return buildResponse(response, 400, "Invalid Minecraft username provided!");
            }
        }

        try {
            SkyBlockProfileReply profile = app.getHypixel().getMostProgressedSkyBlockProfileFromUsername(username).get(10, TimeUnit.SECONDS);
            PlayerReply player = app.getHypixel().getPlayerByName(username).get(10, TimeUnit.SECONDS);
            UUID uuid = app.getHypixel().getUUIDFromName(username);

            JsonObject member = profile.getProfile().getAsJsonObject("members").getAsJsonObject(uuid.toString().replace("-", ""));

            JsonObject data = new JsonObject();
            data.addProperty("uuid", formatUUID(player.getPlayer().get("uuid").getAsString()));
            data.addProperty("username", player.getPlayer().get("displayname").getAsString());
            data.add("profile", profile.getProfile());

            JsonObject stats = new JsonObject();
            stats.add("skills", StatisticsChecker.SKILLS.checkUser(player, profile, member).toJson());
            stats.add("slayer", StatisticsChecker.SLAYER.checkUser(player, profile, member).toJson());
            data.add("stats", stats);

            return buildDataResponse(response, 200, data);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof FriendlyException && e.getLocalizedMessage().contains("Failed to find any valid SkyBlock profiles")) {
                return buildResponse(response, 410, "The requested user doesn't have any SkyBlock profiles!");
            }

            throw e;
        }
    }

    private String formatUUID(String uuid) {
        return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
    }
}
