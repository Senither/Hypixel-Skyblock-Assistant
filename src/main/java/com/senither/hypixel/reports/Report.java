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
import net.hypixel.api.reply.GuildReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.HashSet;
import java.util.UUID;

public class Report {

    private final UUID id;
    private final long discordId;
    private final HashSet<PlayerReport> playerReports = new HashSet<>();
    private final GuildController.GuildEntry guildEntry;
    private final GuildReply guildReply;

    Report(GuildController.GuildEntry guildEntry, GuildReply guildReply, UUID uniqueId) {
        this.id = uniqueId;
        this.discordId = guildEntry.getDiscordId();
        this.guildEntry = guildEntry;
        this.guildReply = guildReply;
    }

    public void createPlayerReport(UnfinishedPlayerReport unfinishedPlayerReport, UUID uuid, SkyBlockProfileReply profileReply) {
        playerReports.add(new PlayerReport(unfinishedPlayerReport.getUsername(), uuid, guildEntry, guildReply, profileReply));
    }

    public UUID getId() {
        return id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public HashSet<PlayerReport> getPlayerReports() {
        return playerReports;
    }
}
