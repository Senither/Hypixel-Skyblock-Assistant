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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerReport {

    private final String username;
    private final String profileName;
    private final UUID uuid;
    private final LinkedHashMap<RankRequirementType, RankCheckResponse> checks;
    private final GuildReply.Guild.Rank rank;

    PlayerReport(String username, UUID uuid, GuildController.GuildEntry guildEntry, GuildReply guildReply, SkyBlockProfileReply profileReply) {
        this.username = username;
        this.uuid = uuid;

        this.profileName = profileReply.getProfile().get("cute_name").getAsString();

        this.checks = new LinkedHashMap<>();
        HashMap<RankRequirementType, GuildReply.Guild.Rank> rankQualifiers = new HashMap<>();
        for (RankRequirementType type : RankRequirementType.values()) {
            try {
                RankCheckResponse response = type.getChecker().getRankForUser(guildEntry, guildReply, profileReply, uuid);

                this.checks.put(type, response);

                if (response == null || response.getRank() == null) {
                    continue;
                }
                rankQualifiers.put(type, response.getRank());
            } catch (Exception e) {
                this.checks.put(type, new RankCheckResponse(null, new HashMap<String, Object>() {{
                    put("exception", e);
                }}));
            }
        }

        GuildReply.Guild.Rank rankQualifier = null;
        List<GuildReply.Guild.Rank> sortedRanks = guildReply.getGuild().getRanks().stream()
            .sorted((o1, o2) -> o2.getPriority() - o1.getPriority())
            .collect(Collectors.toList());

        RANK_LOOP:
        for (GuildReply.Guild.Rank rank : sortedRanks) {
            if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
                continue;
            }

            GuildController.GuildEntry.RankRequirement requirements = guildEntry.getRankRequirements().get(rank.getName());
            for (RankRequirementType type : RankRequirementType.values()) {
                if (!type.getChecker().hasRequirementsSetup(requirements)) {
                    continue;
                }

                if (!rankQualifiers.containsKey(type)) {
                    continue RANK_LOOP;
                }

                GuildReply.Guild.Rank qualifierRank = rankQualifiers.get(type);
                if (rank.getPriority() > qualifierRank.getPriority()) {
                    continue RANK_LOOP;
                }
            }

            rankQualifier = rank;
            break;
        }

        this.rank = rankQualifier;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileName() {
        return profileName;
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
