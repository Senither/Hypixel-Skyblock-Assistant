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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.servlet.SparkRoute;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.GuildReply;
import spark.Request;
import spark.Response;

import java.util.concurrent.TimeUnit;

public class GetGuildRoute extends SparkRoute {

    public static final Cache<String, GuildReply> guildCache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build();

    public GetGuildRoute(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!isAuthorized(request)) {
            return generateUnauthornizedResponse(response);
        }

        String name = request.params("name").trim();
        String cacheKey = (isId(name) ? "uuid-" : "name-") + name.toLowerCase();

        GuildReply guild = guildCache.getIfPresent(cacheKey);
        if (guild == null) {
            HypixelAPI client = app.getHypixel().getClientContainer().getNextClient();

            guild = isId(name)
                ? client.getGuildById(name).get(5, TimeUnit.SECONDS)
                : client.getGuildByName(String.join("+", name.split(" "))).get(5, TimeUnit.SECONDS);

            guildCache.put(cacheKey, guild);
        }

        if (!guild.isSuccess() || guild.getGuild() == null) {
            return buildResponse(response, 404, "No guild were found with the given ID or name");
        }
        return buildDataResponse(response, 200, convertGuildToJsonObject(guild));
    }

    private JsonObject convertGuildToJsonObject(GuildReply guild) {
        JsonObject root = new JsonObject();
        root.addProperty("id", guild.getGuild().get_id());
        root.addProperty("name", guild.getGuild().getName());
        root.addProperty("tag", guild.getGuild().getTag());
        root.addProperty("description", guild.getGuild().getDescription());
        root.addProperty("publicly_listed", guild.getGuild().getPubliclyListed());
        root.addProperty("joinable", guild.getGuild().getJoinable());
        root.addProperty("experience", guild.getGuild().getExp());
        root.addProperty("legacy_ranking", guild.getGuild().getLegacyRanking());

        JsonArray ranks = new JsonArray();
        guild.getGuild().getRanks().stream()
            .sorted((o1, o2) -> o2.getPriority() > o1.getPriority() ? 1 : -1)
            .forEach(rank -> {
                JsonObject rankObject = new JsonObject();
                rankObject.addProperty("name", rank.getName());
                rankObject.addProperty("tag", rank.getTag());
                rankObject.addProperty("priority", rank.getPriority());

                ranks.add(rankObject);
            });
        root.add("ranks", ranks);

        JsonArray members = new JsonArray();
        guild.getGuild().getMembers()
            .forEach(member -> {
                JsonObject memberObject = new JsonObject();
                memberObject.addProperty("uuid", member.getUuid().toString());
                memberObject.addProperty("rank", member.getRank());

                members.add(memberObject);
            });
        root.add("members", members);

        return root;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean isId(String name) {
        return name.length() == 24
            && name.matches("[0-9|a-z]+");
    }
}
