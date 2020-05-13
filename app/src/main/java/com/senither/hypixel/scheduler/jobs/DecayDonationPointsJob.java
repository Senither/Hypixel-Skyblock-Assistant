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

package com.senither.hypixel.scheduler.jobs;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.scheduler.Job;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.time.Carbon;
import net.hypixel.api.reply.GuildReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class DecayDonationPointsJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(DecayDonationPointsJob.class);

    public DecayDonationPointsJob(SkyblockAssistant app) {
        super(app, 1, 60, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        try {
            for (DataRow dataRow : app.getDatabaseManager().query(
                "SELECT `discord_id` FROM `guilds` WHERE `donation_time` IS NOT NULL AND `donation_points` IS NOT NULL;"
            )) {
                GuildController.GuildEntry guild = GuildController.getGuildById(app.getDatabaseManager(), dataRow.getLong("discord_id"));
                if (guild == null) {
                    continue;
                }

                GuildReply guildReply = app.getHypixel().getGson().fromJson(guild.getData(), GuildReply.class);
                if (guildReply == null) {
                    continue;
                }

                HashSet<String> memberIds = new HashSet<>();
                for (GuildReply.Guild.Member member : guildReply.getGuild().getMembers()) {
                    memberIds.add(member.getUuid().toString());
                }

                log.debug("Updating donation points for {}", guild.getDiscordId());

                Carbon time = Carbon.now().subHours(guild.getDonationTime());
                app.getDatabaseManager().queryUpdate(String.format(
                    "UPDATE `donation_points` SET `points` = `points` - ?, `last_checked_at` = ? WHERE `discord_id` = ? AND `last_checked_at` < ? AND `uuid` IN (%s)",
                    "'" + String.join("', '", memberIds) + "'"
                ), guild.getDonationPoints(), Carbon.now(), guild.getDiscordId(), time);
            }
        } catch (SQLException e) {
            log.error("An SQL exception where thrown while trying to update donation points: {}", e.getMessage(), e);
        } finally {
            try {
                app.getDatabaseManager().queryUpdate("UPDATE `donation_points` SET `points` = 0 WHERE `points` < 0");
            } catch (SQLException e) {
                log.error("An SQL exception where thrown while trying to reset donation points back to zero: {}", e.getMessage(), e);
            }
        }
    }
}
