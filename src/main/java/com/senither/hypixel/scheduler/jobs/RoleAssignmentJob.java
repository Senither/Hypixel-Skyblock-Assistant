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
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.hypixel.api.reply.GuildReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class RoleAssignmentJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentJob.class);

    public RoleAssignmentJob(SkyblockAssistant app) {
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

                GuildReply guildReply = app.getHypixel().getAPI().getGuildById(row.getString("id")).get(30, TimeUnit.SECONDS);
                if (guildReply == null || guildReply.getGuild() == null) {
                    continue;
                }

                app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `data` = ?, last_updated_at = NOW() WHERE `id` = ?",
                    app.getHypixel().getGson().toJson(guildReply), row.getString("id")
                );

                GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), guild.getIdLong());
                if (guildEntry == null) {
                    continue;
                }

                try {
                    handleGuildRoleAssignments(guild, guildReply, guildEntry);
                } catch (Exception e) {
                    log.error("An error was thrown in the role assignment job while trying to handle guild with an ID of {}, error: {}",
                        guild.getId(), e.getMessage(), e
                    );
                }

                Thread.sleep(500L);
            }
        } catch (SQLException e) {
            log.error("An SQL Exception where thrown while trying to do role mass assignments, error: {}",
                e.getMessage(), e
            );
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("An error occurred while trying to get guild information from the Hypixel API, error: {}",
                e.getMessage(), e
            );
        }
    }

    private void handleGuildRoleAssignments(Guild guild, GuildReply guildReply, GuildController.GuildEntry guildEntry) throws SQLException {
        GuildReply.Guild hypixelGuild = guildReply.getGuild();

        // Populates the Discord Roles cache list
        HashMap<String, Role> discordRoles = new HashMap<>();
        for (GuildReply.Guild.Member member : hypixelGuild.getMembers()) {
            if ("Guild Master".equalsIgnoreCase(member.getRank())) {
                continue;
            }

            if (discordRoles.containsKey(member.getRank())) {
                continue;
            }

            List<Role> rolesByName = guild.getRolesByName(member.getRank(), false);
            if (rolesByName.isEmpty()) {
                continue;
            }

            if (!guild.getSelfMember().canInteract(rolesByName.get(0))) {
                continue;
            }

            discordRoles.put(member.getRank(), rolesByName.get(0));
        }

        List<String> memberIds = new ArrayList<>();
        guild.getMembers().forEach(member -> memberIds.add(member.getId()));

        StringBuilder stringifiedParams = new StringBuilder();
        for (String ignored : memberIds) {
            stringifiedParams.append("?, ");
        }

        // Gets every verified user from the guild from the database.
        Collection rows = app.getDatabaseManager().query(String.format(
            "SELECT * FROM `uuids` WHERE `discord_id` IN (%s)",
            stringifiedParams.toString().substring(0, stringifiedParams.length() - 2)
        ), memberIds.toArray());

        // Sets up the default role that should be given the users if they're not in the guild
        Role defaultRole = guildEntry.getDefaultRole() == null ? null : guild.getRoleById(guildEntry.getDefaultRole());

        for (Member member : guild.getMembers()) {
            if (member.getUser().isBot() || member.getUser().isFake()) {
                continue;
            }

            List<DataRow> dataRows = rows.where("discord_id", member.getIdLong());
            if (dataRows.isEmpty()) {
                markUserAsGuest(guild, member, discordRoles.values(), defaultRole);
                continue;
            }

            DataRow dataRow = dataRows.get(0);
            String memberUUID = dataRow.getString("uuid");

            if (guildEntry.isAutoRename() && !member.getEffectiveName().equals(dataRow.getString("username"))) {
                guild.modifyNickname(member, dataRow.getString("username")).queue();
            }

            GuildReply.Guild.Member guildMember;
            try {
                guildMember = hypixelGuild.getMembers().stream()
                    .filter(filterMember -> filterMember.getUuid().toString().equals(memberUUID))
                    .findFirst().orElseGet(null);
            } catch (NullPointerException ignored) {
                guildMember = null;
            }

            if (guildMember == null) {
                markUserAsGuest(guild, member, discordRoles.values(), defaultRole);
                continue;
            }

            Role role = discordRoles.getOrDefault(guildMember.getRank(), null);
            if (role == null) {
                continue;
            }

            List<Role> rolesToRemove = discordRoles.values().stream()
                .filter(filteringRole -> filteringRole.getIdLong() != role.getIdLong())
                .collect(Collectors.toList());

            if (defaultRole != null) {
                rolesToRemove.add(defaultRole);
            }

            if (hasRole(member, role) && !hasRoles(member, rolesToRemove)) {
                continue;
            }

            guild.modifyMemberRoles(member, Collections.singletonList(role), rolesToRemove).queue(null, throwable -> {
                log.error("Failed to assign {} role to {} due to an error: {}",
                    role.getName(), member.getEffectiveName(), throwable.getMessage(), throwable
                );
            });
        }
    }

    private void markUserAsGuest(Guild guild, Member member, java.util.Collection<Role> values, Role defaultRole) {
        if (defaultRole == null) {
            return;
        }

        if (hasRole(member, defaultRole) && !hasRoles(member, values)) {
            return;
        }

        guild.modifyMemberRoles(member, Collections.singletonList(defaultRole), values).queue();
    }

    private boolean hasRoles(Member member, java.util.Collection<Role> roles) {
        for (Role role : roles) {
            if (!hasRole(member, role)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRole(Member member, Role role) {
        for (Role memberRole : member.getRoles()) {
            if (memberRole.getId().equals(role.getId())) {
                return true;
            }
        }
        return false;
    }
}
