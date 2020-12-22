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

package com.senither.hypixel.hypixel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.hypixel.Response;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.hypixel.bazaar.BazaarProductReply;
import com.senither.hypixel.hypixel.response.*;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.time.Carbon;
import net.dv8tion.jda.api.entities.User;
import net.hypixel.api.adapters.DateTimeTypeAdapter;
import net.hypixel.api.adapters.UUIDTypeAdapter;
import net.hypixel.api.reply.AbstractReply;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class Hypixel {

    private static final Logger log = LoggerFactory.getLogger(Hypixel.class);

    public static final Cache<String, UUID> usernameToUuidCache = CacheBuilder.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .recordStats()
        .build();

    public static final Cache<UUID, Long> uuidToDiscordIdCache = CacheBuilder.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .recordStats()
        .build();

    public static final Cache<UUID, String> uuidToUsernameCache = CacheBuilder.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .recordStats()
        .build();

    public static final Cache<String, AbstractReply> replyCache = CacheBuilder.newBuilder()
        .expireAfterWrite(90, TimeUnit.SECONDS)
        .recordStats()
        .build();

    public static final Cache<String, Response> responseCache = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .recordStats()
        .build();

    public static final Cache<String, AuctionHouseResponse> auctionsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .recordStats()
        .build();

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
        .registerTypeAdapter(ZonedDateTime.class, new DateTimeTypeAdapter())
        .create();

    private static final Pattern minecraftUsernameRegex = Pattern.compile("^\\w+$", Pattern.CASE_INSENSITIVE);

    private final SkyblockAssistant app;
    private final ClientContainer clientContainer;
    private final HttpClient httpClient;

    public Hypixel(SkyblockAssistant app) {
        this.app = app;

        this.httpClient = HttpClientBuilder.create().build();
        this.clientContainer = new ClientContainer(app);
    }

    public boolean isValidMinecraftUsername(@Nonnull String username) {
        return username.length() > 2 && username.length() < 17 && minecraftUsernameRegex.matcher(username).find();
    }

    public ClientContainer getClientContainer() {
        return clientContainer;
    }

    public Gson getGson() {
        return gson;
    }

    public CompletableFuture<PlayerReply> getPlayerByName(String name) {
        return getPlayerByName(name, false);
    }

    public CompletableFuture<PlayerReply> getPlayerByName(String name, boolean ignoreDatabaseCache) {
        CompletableFuture<PlayerReply> future = new CompletableFuture<>();

        final String cacheKey = "player-name-" + name.toLowerCase();
        try {
            UUID uuid = getUUIDFromName(name);
            if (uuid == null) {
                handleResponseException(future, new FriendlyException("Failed to find a valid UUID for the given username!"));
                return future;
            }

            AbstractReply cachedPlayerProfile = replyCache.getIfPresent(cacheKey);
            if (cachedPlayerProfile instanceof PlayerReply) {
                log.debug("Found player profile for {} using the in-memory cache (ID: {})", name, uuid.toString());

                future.complete((PlayerReply) cachedPlayerProfile);
                return future;
            }

            final boolean[] hasDatabaseEntry = {false};

            if (!ignoreDatabaseCache) {
                Collection result = app.getDatabaseManager().query("SELECT `data`, `last_updated_at` FROM `players` WHERE `uuid` = ?", uuid.toString());
                if (!result.isEmpty()) {
                    Carbon lastUpdatedAt = result.first().getTimestamp("last_updated_at");
                    if (lastUpdatedAt.addMinutes(30).isFuture()) {
                        PlayerReply playerReply = gson.fromJson(result.get(0).getString("data"), PlayerReply.class);
                        if (playerReply != null && playerReply.getPlayer() != null) {
                            log.debug("Found player profile for {} using the database cache (ID: {})", name, uuid);

                            replyCache.put(cacheKey, playerReply);
                            future.complete(playerReply);

                            return future;
                        }
                    }
                    hasDatabaseEntry[0] = true;
                }
            }

            log.debug("Requesting for player profile for \"{}\" using the API", name);

            getClientContainer().getNextClient().getPlayerByUuid(uuid).whenCompleteAsync((playerReply, throwable) -> {
                if (throwable != null) {
                    handleResponseException(future, throwable);
                    return;
                }

                replyCache.put(cacheKey, playerReply);

                try {
                    if (ignoreDatabaseCache) {
                        hasDatabaseEntry[0] = !app.getDatabaseManager().query(
                            "SELECT `data` FROM `players` WHERE `uuid` = ?", uuid.toString()
                        ).isEmpty();
                    }

                    if (hasDatabaseEntry[0]) {
                        app.getDatabaseManager().queryUpdate(
                            "UPDATE `players` SET `data` = ?, `last_updated_at` = ? WHERE `uuid` = ?",
                            gson.toJson(playerReply), Carbon.now(), uuid.toString()
                        );
                    } else {
                        app.getDatabaseManager().queryInsert(
                            "INSERT INTO `players` SET `uuid` = ?, `data` = ?, `created_at` = NOW(), `last_updated_at` = ?",
                            uuid.toString(), gson.toJson(playerReply), Carbon.now()
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to create/update player data for {}, error: {}",
                        uuid.toString(), e.getMessage(), e);
                }

                future.complete(playerReply);
            });
        } catch (SQLException e) {
            handleResponseException(future, e);
        }

        return future;
    }

    public CompletableFuture<SkyBlockProfileReply> getSelectedSkyBlockProfileFromUsername(String name) {
        CompletableFuture<SkyBlockProfileReply> future = new CompletableFuture<>();

        getPlayerByName(name).whenComplete((playerReply, throwable) -> {
            if (throwable != null) {
                log.debug("Failed to get selected skyblock profile for \"{}\" due to an exception while getting Hypixel profile.", name);
                handleResponseException(future, throwable);
                return;
            }

            if (playerReply.getPlayer() == null) {
                handleResponseException(future, new FriendlyException("Failed to find any valid SkyBlock profiles!"));
                return;
            }

            try {
                List<SkyBlockProfileReply> skyBlockProfileReplies = prepareSkyBlockProfiles(playerReply);
                if (skyBlockProfileReplies.isEmpty()) {
                    log.debug("Failed to get selected skyblock profile for \"{}\" due to having found no valid profiles.", name);

                    handleResponseException(future, new FriendlyException("Failed to find any valid SkyBlock profiles!"));
                    return;
                }

                //noinspection ConstantConditions
                SkyBlockProfileReply skyBlockProfileReply = skyBlockProfileReplies.stream()
                    .sorted((profileOne, profileTwo) -> {
                        return getLastSaveFromMember(profileOne.getProfile().getAsJsonObject("members").getAsJsonObject(
                            playerReply.getPlayer().get("uuid").getAsString()
                        )) < getLastSaveFromMember(profileTwo.getProfile().getAsJsonObject("members").getAsJsonObject(
                            playerReply.getPlayer().get("uuid").getAsString()
                        )) ? 1 : -1;
                    }).findFirst().get();

                log.debug("Found selected SkyBlock profile for \"{}\" it was \"{}\" with UUID \"{}\"",
                    name, skyBlockProfileReply.getProfile().get("cute_name").getAsString(), skyBlockProfileReply.getProfile().get("profile_id").getAsString()
                );

                future.complete(skyBlockProfileReply);
            } catch (NullPointerException e) {
                handleResponseException(future, new FriendlyException("Found no SkyBlock profiles for " + name));
            } catch (Exception e) {
                handleResponseException(future, e);
            }
        });

        return future;
    }

    public CompletableFuture<SkyBlockProfileReply> getMostProgressedSkyBlockProfileFromUsername(String name) {
        CompletableFuture<SkyBlockProfileReply> future = new CompletableFuture<>();

        getPlayerByName(name).whenComplete((playerReply, throwable) -> {
            if (throwable != null) {
                log.debug("Failed to get most progressed skyblock profile for \"{}\" due to an exception while getting Hypixel profile.", name);
                handleResponseException(future, throwable);
                return;
            }

            if (playerReply.getPlayer() == null) {
                handleResponseException(future, new FriendlyException("Failed to find any valid SkyBlock profiles!"));
                return;
            }

            try {
                List<SkyBlockProfileReply> skyBlockProfileReplies = prepareSkyBlockProfiles(playerReply);
                if (skyBlockProfileReplies.isEmpty()) {
                    log.debug("Failed to get most progressed skyblock profile for \"{}\" due to having found no valid profiles.", name);

                    handleResponseException(future, new FriendlyException("Failed to find any valid SkyBlock profiles!"));
                    return;
                }

                final String uuid = playerReply.getPlayer().get("uuid").getAsString();

                SkyBlockProfileReply skyBlockProfileReply = skyBlockProfileReplies.stream()
                    .max(Comparator.comparingDouble(profile -> getSkyblockProfileScore(playerReply, profile, uuid)))
                    .orElseThrow(() -> new FriendlyException("Failed to find any valid SkyBlock profiles!"));

                log.debug("Found selected SkyBlock profile for \"{}\" it was \"{}\" with UUID \"{}\"",
                    name, skyBlockProfileReply.getProfile().get("cute_name").getAsString(), skyBlockProfileReply.getProfile().get("profile_id").getAsString()
                );

                future.complete(skyBlockProfileReply);
            } catch (NullPointerException e) {
                handleResponseException(future, new FriendlyException("Failed to find any valid SkyBlock profiles!"));
            } catch (Exception e) {
                handleResponseException(future, e);
            }
        });

        return future;
    }

    public CompletableFuture<SkyBlockProfileReply> getSkyBlockProfile(String name) {
        CompletableFuture<SkyBlockProfileReply> future = new CompletableFuture<>();

        final String cacheKey = "skyblock-profile-" + name;

        AbstractReply cachedSkyBlockProfile = replyCache.getIfPresent(cacheKey);
        if (cachedSkyBlockProfile instanceof SkyBlockProfileReply) {
            log.debug("Found SkyBlock profile {} using the in-memory cache", name);

            ((SkyBlockProfileReply) cachedSkyBlockProfile).getProfile().addProperty("isFromCache", true);

            future.complete((SkyBlockProfileReply) cachedSkyBlockProfile);
            return future;
        }

        boolean hasDatabaseEntry = false;

        try {
            Collection result = app.getDatabaseManager().query("SELECT `data`, `last_updated_at` FROM `profiles` WHERE `uuid` = ?", name);
            if (!result.isEmpty()) {
                Carbon lastUpdatedAt = result.first().getTimestamp("last_updated_at");
                if (lastUpdatedAt.addMinutes(5).isFuture()) {
                    SkyBlockProfileReply skyblockProfile = gson.fromJson(result.get(0).getString("data"), SkyBlockProfileReply.class);
                    if (skyblockProfile != null && skyblockProfile.getProfile() != null) {
                        log.debug("Found SkyBlock profile for {} using the database cache", name);

                        skyblockProfile.getProfile().addProperty("isFromCache", true);

                        replyCache.put(cacheKey, skyblockProfile);
                        future.complete(skyblockProfile);

                        return future;
                    }
                }
                hasDatabaseEntry = true;
            }
        } catch (SQLException e) {
            log.error("An exception were thrown while trying to get the SkyBlock profile from the database cache, error: {}",
                e.getMessage(), e
            );
        }

        log.debug("Requesting for SkyBlock profile with an ID of {} from the API", name);

        boolean finalHasDatabaseEntry = hasDatabaseEntry;
        clientContainer.getNextClient().getSkyBlockProfile(name).whenComplete((skyBlockProfileReply, throwable) -> {
            if (throwable != null) {
                handleResponseException(future, throwable);
                return;
            }

            replyCache.put(cacheKey, skyBlockProfileReply);

            try {
                if (finalHasDatabaseEntry) {
                    app.getDatabaseManager().queryUpdate("UPDATE `profiles` SET `data` = ?, `last_updated_at` = ? WHERE `uuid` = ?",
                        gson.toJson(skyBlockProfileReply), Carbon.now(), name
                    );
                } else {
                    app.getDatabaseManager().queryInsert("INSERT INTO `profiles` SET `uuid` = ?, `data` = ?, `last_updated_at` = ?",
                        name, gson.toJson(skyBlockProfileReply), Carbon.now()
                    );
                }
            } catch (Exception e) {
                log.error("Failed to create/update profile data for {}, error: {}",
                    name, e.getMessage(), e
                );
            }

            future.complete(skyBlockProfileReply);
        });

        return future;
    }

    public CompletableFuture<GuildReply> getGuildByPlayer(String uuid) {
        CompletableFuture<GuildReply> future = new CompletableFuture<>();

        final String cacheKey = "skyblock-guild-player-" + uuid;

        AbstractReply cachedSkyBlockGuild = replyCache.getIfPresent(cacheKey);
        if (cachedSkyBlockGuild instanceof GuildReply) {
            log.debug("Found SkyBlock Guild from player {} using the in-memory cache", uuid);

            future.complete((GuildReply) cachedSkyBlockGuild);
            return future;
        }

        log.debug("Requesting for SkyBlock Guild from player {} from the API", uuid);

        clientContainer.getNextClient().getGuildByPlayer(uuid).whenComplete((skyBlockGuildReply, throwable) -> {
            if (throwable != null) {
                handleResponseException(future, throwable);
                return;
            }

            replyCache.put(cacheKey, skyBlockGuildReply);

            future.complete(skyBlockGuildReply);
        });

        return future;
    }

    public CompletableFuture<GuildReply> getGuildByName(String name) {
        CompletableFuture<GuildReply> future = new CompletableFuture<>();

        final String cacheKey = "skyblock-guild-" + name.trim().toLowerCase();

        AbstractReply cachedSkyBlockGuild = replyCache.getIfPresent(cacheKey);
        if (cachedSkyBlockGuild instanceof GuildReply) {
            log.debug("Found SkyBlock Guild {} using the in-memory cache", name);

            future.complete((GuildReply) cachedSkyBlockGuild);
            return future;
        }

        try {
            Collection result = app.getDatabaseManager().query("SELECT `data` FROM `guilds` WHERE `name` = ?", name);
            if (!result.isEmpty()) {
                GuildReply skyblockGuild = gson.fromJson(result.get(0).getString("data"), GuildReply.class);
                if (skyblockGuild != null && skyblockGuild.getGuild() != null) {
                    log.debug("Found SkyBlock Guild for {} using the database cache", name);

                    replyCache.put(cacheKey, skyblockGuild);
                    future.complete(skyblockGuild);

                    return future;
                }
            }
        } catch (SQLException e) {
            log.error("An exception were thrown while trying to get the SkyBlock profile from the database cache, error: {}",
                e.getMessage(), e
            );
        }

        log.debug("Requesting for SkyBlock Guild with a name of {} from the API", name);

        clientContainer.getNextClient().getGuildByName(name).whenComplete((skyBlockGuildReply, throwable) -> {
            if (throwable != null) {
                handleResponseException(future, throwable);
                return;
            }

            replyCache.put(cacheKey, skyBlockGuildReply);

            future.complete(skyBlockGuildReply);
        });

        return future;
    }

    public AuctionHouseResponse getAuctionsFromProfile(String profileId) {
        AuctionHouseResponse cachedAuctionResponse = auctionsCache.getIfPresent(profileId);
        if (cachedAuctionResponse != null) {
            log.debug("Found SkyBlock Auctions for {} using the in-memory cache", profileId);

            return cachedAuctionResponse;
        }

        log.debug("Requesting SkyBlock Auctions for {} from the API", profileId);

        UUID randomApiKey = clientContainer.getNextClient().getApiKey();

        try {
            AuctionHouseResponse auctionHouseResponse = httpClient.execute(new HttpGet(String.format(
                "https://api.hypixel.net/skyblock/auction?key=%s&profile=%s",
                randomApiKey.toString(), profileId
            )), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, AuctionHouseResponse.class);
            });

            if (auctionHouseResponse == null || !auctionHouseResponse.isSuccess()) {
                return null;
            }

            auctionsCache.put(profileId, auctionHouseResponse);

            return auctionHouseResponse;
        } catch (IOException e) {
            return null;
        }
    }

    public BazaarProductReply getBazaarProducts() {
        final String cacheKey = "skyblock-bazaar-products";

        AbstractReply bazaarProductsCacheReply = replyCache.getIfPresent(cacheKey);
        if (bazaarProductsCacheReply instanceof BazaarProductReply) {
            log.debug("Found Bazaar Products using the in-memory cache");

            return (BazaarProductReply) bazaarProductsCacheReply;
        }

        log.debug("Requesting for SkyBlock Bazaar Products from the API");

        UUID randomApiKey = clientContainer.getNextClient().getApiKey();

        try {
            BazaarProductReply bazaarProductReply = httpClient.execute(new HttpGet(String.format(
                "https://api.hypixel.net/skyblock/bazaar?key=%s", randomApiKey.toString()
            )), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, BazaarProductReply.class);
            });

            if (bazaarProductReply == null || !bazaarProductReply.isSuccess()) {
                return null;
            }

            replyCache.put(cacheKey, bazaarProductReply);

            return bazaarProductReply;
        } catch (IOException e) {
            return null;
        }
    }

    public PlayerLeaderboardResponse getPlayerLeaderboard() {
        final String cacheKey = "skyblock-player-leaderboard";

        Response cachedLeaderboard = responseCache.getIfPresent(cacheKey);
        if (cachedLeaderboard instanceof PlayerLeaderboardResponse) {
            log.debug("Found Player Leaderboard using the in-memory cache");

            return (PlayerLeaderboardResponse) cachedLeaderboard;
        }

        log.debug("Requesting for Player Leaderboard from the API");

        try {
            PlayerLeaderboardResponse leaderboardResponse = httpClient.execute(new HttpGet(app.getConfiguration().getLeaderboardUri() + "/players"), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, PlayerLeaderboardResponse.class);
            });

            if (leaderboardResponse == null) {
                return null;
            }

            responseCache.put(cacheKey, leaderboardResponse);

            return leaderboardResponse;
        } catch (IOException e) {
            return null;
        }
    }

    public GuildLeaderboardResponse getGuildLeaderboard() {
        final String cacheKey = "skyblock-leaderboard";

        Response cachedLeaderboard = responseCache.getIfPresent(cacheKey);
        if (cachedLeaderboard instanceof GuildLeaderboardResponse) {
            log.debug("Found Guild Leaderboard using the in-memory cache");

            return (GuildLeaderboardResponse) cachedLeaderboard;
        }

        log.debug("Requesting for Guild Leaderboard from the API");

        try {
            GuildLeaderboardResponse leaderboardResponse = httpClient.execute(new HttpGet(app.getConfiguration().getLeaderboardUri()), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, GuildLeaderboardResponse.class);
            });

            if (leaderboardResponse == null) {
                return null;
            }

            responseCache.put(cacheKey, leaderboardResponse);

            return leaderboardResponse;
        } catch (IOException e) {
            return null;
        }
    }

    public GuildMetricsResponse getGuildLeaderboardMetrics(String guildId) {
        final String cacheKey = "skyblock-metrics-leaderboard-" + guildId;

        Response cachedLeaderboard = responseCache.getIfPresent(cacheKey);
        if (cachedLeaderboard instanceof GuildMetricsResponse) {
            log.debug("Found metrics for {} using the in-memory cache", guildId);

            return (GuildMetricsResponse) cachedLeaderboard;
        }

        log.debug("Requesting for Guild metrics for {} from the API", guildId);

        try {
            GuildMetricsResponse leaderboardResponse = httpClient.execute(new HttpGet(app.getConfiguration().getLeaderboardUri() + "/metrics/" + guildId), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, GuildMetricsResponse.class);
            });

            if (leaderboardResponse == null) {
                return null;
            }

            responseCache.put(cacheKey, leaderboardResponse);

            return leaderboardResponse;
        } catch (IOException e) {
            return null;
        }
    }

    public PlayerLeaderboardResponse getGuildPlayersLeaderboard(String guildId) {
        final String cacheKey = "skyblock-player-leaderboard-" + guildId;

        Response cachedLeaderboard = responseCache.getIfPresent(cacheKey);
        if (cachedLeaderboard instanceof PlayerLeaderboardResponse) {
            log.debug("Found Player Leaderboard for {} using the in-memory cache", guildId);

            return (PlayerLeaderboardResponse) cachedLeaderboard;
        }

        log.debug("Requesting for Player Leaderboard for {} from the API", guildId);

        try {
            PlayerLeaderboardResponse leaderboardResponse = httpClient.execute(new HttpGet(app.getConfiguration().getLeaderboardUri() + "/players/" + guildId), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, PlayerLeaderboardResponse.class);
            });

            if (leaderboardResponse == null) {
                return null;
            }

            responseCache.put(cacheKey, leaderboardResponse);

            return leaderboardResponse;
        } catch (IOException e) {
            return null;
        }
    }

    public boolean isLeaderboardApiValid() {
        try {
            LeaderboardStatsResponse response = httpClient.execute(new HttpGet(app.getConfiguration().getLeaderboardUri() + "/stats"), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, LeaderboardStatsResponse.class);
            });

            if (response == null || !response.isSuccess() || response.getData() == null) {
                return false;
            }

            return response.getData().getGuilds() > 0 && response.getData().getPlayers() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getDiscordIdFromUUID(UUID uuid) {
        Long discordId = uuidToDiscordIdCache.getIfPresent(uuid);
        if (discordId != null) {
            log.debug("Found Discord ID for {} using the in-memory cache (ID: {})", uuid, discordId);
            return discordId;
        }

        try {
            Collection result = app.getDatabaseManager().query("SELECT `discord_id` FROM `uuids` WHERE `uuid` = ?", uuid);
            if (!result.isEmpty()) {
                discordId = result.get(0).getLong("discord_id");
                if (discordId <= 0) {
                    return null;
                }

                uuidToDiscordIdCache.put(uuid, discordId);
                log.debug("Found Discord ID for {} using the database cache (ID: {})", uuid, discordId);

                return discordId;
            }
        } catch (SQLException e) {
            log.error("An SQL Exception were thrown while trying to get the Discord ID for ");
        }
        return null;
    }

    public UUID getUUIDFromUser(User user) throws SQLException {
        UUID cachedUUID = Command.discordIdToUuidCache.getIfPresent(user.getIdLong());
        if (cachedUUID != null) {
            log.debug("Found UUID for {} using the in-memory cache (ID: {})", user.getAsTag(), cachedUUID);
            return cachedUUID;
        }

        Collection result = app.getDatabaseManager().query("SELECT `uuid` FROM `uuids` WHERE `discord_id` = ?", user.getIdLong());
        if (!result.isEmpty()) {
            UUID uuid = UUID.fromString(result.get(0).getString("uuid"));
            Command.discordIdToUuidCache.put(user.getIdLong(), uuid);
            log.debug("Found UUID for {} using the database cache (ID: {})", user, uuid);

            return uuid;
        }

        return null;
    }

    public synchronized UUID getUUIDFromName(String name) throws SQLException {
        UUID cachedUUID = usernameToUuidCache.getIfPresent(name.toLowerCase());
        if (cachedUUID != null) {
            log.debug("Found UUID for {} using the in-memory cache (ID: {})", name, cachedUUID);
            return cachedUUID;
        }

        Collection result = app.getDatabaseManager().query("SELECT `uuid` FROM `uuids` WHERE `username` = ?", name);
        if (!result.isEmpty()) {
            UUID uuid = UUID.fromString(result.get(0).getString("uuid"));
            usernameToUuidCache.put(name.toLowerCase(), uuid);
            log.debug("Found UUID for {} using the database cache (ID: {})", name, uuid);

            return uuid;
        }

        try {
            MojangPlayerUUID mojangPlayer = httpClient.execute(new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name), obj -> {
                String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                return gson.fromJson(content, MojangPlayerUUID.class);
            });

            if (mojangPlayer == null || mojangPlayer.getUUID() == null) {
                return null;
            }

            log.debug("Found UUID for {} using the Mojang API (ID: {})", name, mojangPlayer.getUUID());

            try {
                app.getDatabaseManager().queryInsert("INSERT INTO `uuids` SET `uuid` = ?, `username` = ?",
                    mojangPlayer.getUUID().toString(), name
                );
            } catch (Exception e) {
                log.error("Failed to get UUID for player {}, error: {}",
                    name, e.getMessage(), e
                );
            }

            return mojangPlayer.getUUID();
        } catch (IOException e) {
            log.error("Failed to fetch UUID for {} using the Mojang API, error: {}", name, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // We can ignore this exception since it should only be thrown if
            // the Hypixel API returns null due to the player not existing.
        }

        return null;
    }

    public void forgetUsernameCacheEntry(UUID uuid) {
        uuidToUsernameCache.invalidate(uuid);
    }

    public synchronized String getUsernameFromUuid(UUID uuid) throws SQLException {
        String cachedUsername = uuidToUsernameCache.getIfPresent(uuid);
        if (cachedUsername != null) {
            log.debug("Found Username for {} using the in-memory cache (Username: {})", uuid, cachedUsername);
            return cachedUsername;
        }

        Collection result = app.getDatabaseManager().query("SELECT `username` FROM `uuids` WHERE `uuid` = ?", uuid.toString());
        if (!result.isEmpty()) {
            String username = result.get(0).getString("username");
            uuidToUsernameCache.put(uuid, username);
            log.debug("Found Username for {} using the database cache (Username: {})", uuid, username);

            return username;
        }

        try {
            PlayerReply playerReply = getClientContainer().getNextClient().getPlayerByUuid(uuid).get(10, TimeUnit.SECONDS);

            if (playerReply == null || playerReply.getPlayer() == null) {
                return null;
            }

            String username = playerReply.getPlayer().get("displayname").getAsString();

            uuidToUsernameCache.put(uuid, username);

            log.debug("Found Username for {} using the Hypixel API (Username: {})", uuid, username);

            try {
                app.getDatabaseManager().queryInsert("INSERT INTO `uuids` SET `uuid` = ?, `username` = ?",
                    uuid, username
                );

                app.getDatabaseManager().queryInsert("INSERT INTO `players` SET `uuid` = ?, `data` = ?",
                    uuid, getGson().toJson(playerReply)
                );
            } catch (Exception e) {
                log.error("Failed to get Username from UUID for player {}, error: {}",
                    uuid.toString(), e.getMessage(), e
                );
            }

            return username;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to fetch Username for {} using the Hypixel API, error: {}", uuid, e.getMessage(), e);
        }

        return null;
    }

    public HypixelRank getRankFromPlayer(PlayerReply playerReply) {
        if (playerReply == null || playerReply.getPlayer() == null) {
            return HypixelRank.getDefaultRank();
        }

        JsonObject player = playerReply.getPlayer();
        if (player.has("monthlyPackageRank") && !player.get("monthlyPackageRank").getAsString().equals("NONE")) {
            return HypixelRank.getFromType(player.get("monthlyPackageRank").getAsString());
        } else if (player.has("newPackageRank")) {
            return HypixelRank.getFromType(player.get("newPackageRank").getAsString());
        } else if (player.has("packageRank")) {
            return HypixelRank.getFromType(player.get("packageRank").getAsString());
        }

        return HypixelRank.DEFAULT;
    }

    private List<SkyBlockProfileReply> prepareSkyBlockProfiles(PlayerReply playerReply) {
        JsonObject profiles = playerReply.getPlayer().getAsJsonObject("stats").getAsJsonObject("SkyBlock").getAsJsonObject("profiles");

        List<SkyBlockProfileReply> skyBlockProfileReplies = new ArrayList<>();
        for (Map.Entry<String, JsonElement> profileEntry : profiles.entrySet()) {
            try {
                SkyBlockProfileReply profileReply = getSkyBlockProfile(profileEntry.getKey()).get(5, TimeUnit.SECONDS);
                if (!profileReply.isSuccess()) {
                    continue;
                }

                profileReply.getProfile().add("cute_name", profileEntry.getValue().getAsJsonObject().get("cute_name"));

                skyBlockProfileReplies.add(profileReply);
            } catch (Exception e) {
                log.error("Failed to get selected profile for {}, error: {}",
                    playerReply.getPlayer().get("displayname").getAsString(), e.getMessage(), e
                );
            }
        }
        return skyBlockProfileReplies;
    }

    private double getSkyblockProfileScore(PlayerReply playerReply, SkyBlockProfileReply profileReply, String uuid) {
        final JsonObject member = profileReply.getProfile().getAsJsonObject("members").getAsJsonObject(uuid);

        return StatisticsChecker.SKILLS.checkUser(playerReply, profileReply, member).calculateTotalWeight()
            .add(StatisticsChecker.SLAYER.checkUser(playerReply, profileReply, member).calculateTotalWeight())
            .add(StatisticsChecker.DUNGEON.checkUser(playerReply, profileReply, member).calculateTotalWeight())
            .getTotalWeight();
    }

    private long getLastSaveFromMember(JsonObject object) {
        return object != null && object.has("last_save") ? object.get("last_save").getAsLong() : Long.MIN_VALUE;
    }

    private void handleResponseException(CompletableFuture<?> future, Throwable throwable) {
        if (throwable instanceof FriendlyException) {
            future.completeExceptionally(throwable);
        } else if (throwable instanceof SSLPeerUnverifiedException) {
            future.completeExceptionally(new FriendlyException("Hypixels API is currently experiencing some issues, please try again later."));
        } else {
            future.completeExceptionally(throwable);
        }
    }
}
