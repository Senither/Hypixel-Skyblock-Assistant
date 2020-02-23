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

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import net.hypixel.api.reply.GuildReply;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.UUID;

public class ReportService {

    private static final LinkedHashMap<Report, HashSet<UnfinishedPlayerReport>> playerQueue = new LinkedHashMap<>();

    public static LinkedHashMap<Report, HashSet<UnfinishedPlayerReport>> getPlayerQueue() {
        return playerQueue;
    }

    public static void resumeUnfinishedReports(SkyblockAssistant app) {
        try {
            Collection rows = app.getDatabaseManager().query("SELECT * FROM `reports` WHERE `finished_at` IS NULL");

            for (DataRow row : rows) {
                UUID uniqueId = UUID.fromString(row.getString("id"));
                long discordId = row.getLong("discord_id");

                GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), discordId);

                GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);
                if (guildReply == null || guildReply.getGuild() == null) {
                    continue;
                }

                HashSet<UnfinishedPlayerReport> unfinishedPlayerReports = new HashSet<>();
                for (GuildReply.Guild.Member member : guildReply.getGuild().getMembers()) {
                    unfinishedPlayerReports.add(new UnfinishedPlayerReport(member));
                }

                playerQueue.put(new Report(guildEntry, guildReply, uniqueId), unfinishedPlayerReports);
            }
        } catch (SQLException e) {
            // TODO: Handle exceptions here in a better way
            e.printStackTrace();
        }
    }

    public static UUID createReportFor(SkyblockAssistant app, GuildController.GuildEntry guild) throws SQLException {
        boolean hasUnfinishedReports = !app.getDatabaseManager().query(
            "SELECT id FROM `reports` WHERE `discord_id` = ? AND `finished_at` IS NULL",
            guild.getDiscordId()
        ).isEmpty();

        if (hasUnfinishedReports) {
            // TODO: Throw an exception here instead, to better represent that the user can't create two reports at the same time.
            return null;
        }

        GuildReply guildReply = app.getHypixel().getGson().fromJson(guild.getData(), GuildReply.class);
        if (guildReply == null || guildReply.getGuild() == null) {
            // TODO: Throw an exception here
            return null;
        }

        HashSet<UnfinishedPlayerReport> unfinishedPlayerReports = new HashSet<>();
        for (GuildReply.Guild.Member member : guildReply.getGuild().getMembers()) {
            unfinishedPlayerReports.add(new UnfinishedPlayerReport(member));
        }

        final UUID uniqueId = UUID.randomUUID();

        app.getDatabaseManager().queryInsert("INSERT INTO `reports` SET `id` = ?, `discord_id` = ?",
            uniqueId, guild.getDiscordId()
        );

        playerQueue.put(new Report(guild, guildReply, uniqueId), unfinishedPlayerReports);

        return uniqueId;
    }
}
