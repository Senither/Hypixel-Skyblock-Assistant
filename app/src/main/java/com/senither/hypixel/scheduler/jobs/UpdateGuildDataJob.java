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
import net.dv8tion.jda.api.entities.Guild;
import net.hypixel.api.reply.GuildReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateGuildDataJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(UpdateGuildDataJob.class);

    public UpdateGuildDataJob(SkyblockAssistant app) {
        super(app, 1, 15, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        try {
            for (DataRow row : app.getDatabaseManager().query("SELECT * FROM `guilds`")) {
                Guild guild = app.getShardManager().getGuildById(row.getString("discord_id"));
                if (guild == null) {
                    continue;
                }

                GuildReply guildReply = app.getHypixel()
                    .getClientContainer()
                    .getNextClient()
                    .getGuildById(row.getString("id"))
                    .get(5, TimeUnit.SECONDS);

                if (guildReply == null || guildReply.getGuild() == null) {
                    continue;
                }

                app.getDatabaseManager().queryUpdate(
                    "UPDATE `guilds` SET `name` = ?, `data` = ?, last_updated_at = NOW() WHERE `id` = ?",
                    guildReply.getGuild().getName(),
                    app.getHypixel().getGson().toJson(guildReply),
                    row.getString("id")
                );

                Thread.sleep(500L);
            }
        } catch (SQLException e) {
            log.error("An SQL Exception where thrown while trying to update guild data, error: {}",
                e.getMessage(), e
            );
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("An error occurred while trying to get guild information from the Hypixel API, error: {}",
                e.getMessage(), e
            );
        }
    }
}
