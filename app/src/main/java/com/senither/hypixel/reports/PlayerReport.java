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

package com.senither.hypixel.reports;

import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.rank.RankCheckResponse;
import com.senither.hypixel.rank.RankRequirementType;
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.UUID;

public class PlayerReport {

    private final String username;
    private final UUID uuid;
    private final LinkedHashMap<RankRequirementType, RankCheckResponse> checks;
    private final GuildReply.Guild.Rank rank;

    PlayerReport(String username, UUID uuid, GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply) {
        this.username = username;
        this.uuid = uuid;

        this.checks = new LinkedHashMap<>();
        HashSet<GuildReply.Guild.Rank> rankQualifiers = new HashSet<>();
        for (RankRequirementType type : RankRequirementType.values()) {
            try {
                RankCheckResponse response = type.getChecker().getRankForUser(guildEntry, guildReply, profileReply, uuid);

                this.checks.put(type, response);

                if (response == null || response.getRank() == null) {
                    rankQualifiers.add(null);
                    continue;
                }
                rankQualifiers.add(response.getRank());
            } catch (Exception e) {
                this.checks.put(type, new RankCheckResponse(null, new HashMap<String, Object>() {{
                    put("exception", e);
                }}));
            }
        }

        GuildReply.Guild.Rank rankQualifier = guildReply.getGuild().getRanks().stream()
            .sorted((o1, o2) -> o2.getPriority() - o1.getPriority())
            .findFirst().get();

        for (GuildReply.Guild.Rank qualifier : rankQualifiers) {
            if (qualifier == null) {
                rankQualifier = null;
                break;
            }

            if (qualifier.getPriority() < rankQualifier.getPriority()) {
                rankQualifier = qualifier;
            }
        }

        this.rank = rankQualifier;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public LinkedHashMap<RankRequirementType, RankCheckResponse> getChecks() {
        return checks;
    }

    public GuildReply.Guild.Rank getRank() {
        return rank;
    }
}
