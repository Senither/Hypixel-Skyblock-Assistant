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

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.scheduler.Job;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.hypixel.HypixelRank;
import com.senither.hypixel.time.Carbon;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.hypixel.api.reply.PlayerReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HypixelRankSynchronizeJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(HypixelRankSynchronizeJob.class);

    public HypixelRankSynchronizeJob(SkyblockAssistant app) {
        super(app, 30, 15, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        final Carbon time = Carbon.now().subMinutes(30);

        try {
            Collection query = app.getDatabaseManager().query(
                "SELECT * FROM `uuids` WHERE `discord_id` AND `last_checked_at` < ? IS NOT NULL ORDER BY `last_checked_at` ASC LIMIT 1;",
                time
            );

            if (query.isEmpty()) {
                return;
            }

            try {
                handleCheckForUser(query.first());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Failed to fetch player data during Hypixel Rank Synchronize Job, error: {}", e.getMessage(), e);
            } finally {
                app.getDatabaseManager().queryUpdate("UPDATE `uuids` SET `last_checked_at` = NOW() WHERE `discord_id` = ?", query.first().get("discord_id"));
            }
        } catch (SQLException e) {
            log.error("Failed to find any user for the synchronize job, error: {}", e.getMessage(), e);

            try {
                app.getDatabaseManager().queryUpdate(
                    "UPDATE `uuids` SET `last_checked_at` = NOW() WHERE `discord_id` IS NOT NULL AND `last_checked_at` < ? ORDER BY `last_checked_at` ASC LIMIT 1;",
                    time
                );
            } catch (SQLException sqlException) {
                log.error("Failed to update last_checked_at data for the last checked row, error: {}", sqlException.getMessage(), sqlException);
            }
        }
    }

    private void handleCheckForUser(DataRow row) throws InterruptedException, ExecutionException, TimeoutException {
        User user = app.getShardManager().getUserById(row.getLong("discord_id"));
        if (user == null) {
            return;
        }

        PlayerReply playerReply = app.getHypixel().getPlayerByName(
            row.getString("username"), true
        ).get(5, TimeUnit.SECONDS);

        HypixelRank rank = app.getHypixel().getRankFromPlayer(playerReply);

        for (Guild guild : app.getShardManager().getGuilds()) {
            Member member = guild.getMember(user);
            if (member == null) {
                continue;
            }

            if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                continue;
            }

            List<Role> rolesToAdd = new ArrayList<>();
            List<Role> rolesToRemove = new ArrayList<>();

            if (!rank.isDefault()) {
                List<Role> rankRolesByName = guild.getRolesByName(rank.getName(), true);
                if (!rankRolesByName.isEmpty()) {
                    rolesToAdd.add(rankRolesByName.get(0));
                }
            }

            List<Role> verifiedRoleByName = guild.getRolesByName(Constants.VERIFY_ROLE, true);
            if (!verifiedRoleByName.isEmpty()) {
                rolesToAdd.add(verifiedRoleByName.get(0));
            }

            for (HypixelRank hypixelRank : HypixelRank.values()) {
                if (rank == hypixelRank || hypixelRank.isDefault()) {
                    continue;
                }

                List<Role> rankRolesByName = guild.getRolesByName(hypixelRank.getName(), true);
                if (!rankRolesByName.isEmpty()) {
                    rolesToRemove.add(rankRolesByName.get(0));
                }
            }

            if (rolesToAdd.isEmpty() && rolesToRemove.isEmpty()) {
                continue;
            }

            guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue(null, null);
        }
    }
}
