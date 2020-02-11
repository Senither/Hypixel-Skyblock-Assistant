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

package com.senither.hypixel.contracts.rank;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.rank.RankRequirementType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.sql.SQLException;

public abstract class RankCommandHandler {

    protected RankRequirementType rankType;

    public void setRankType(RankRequirementType rankType) {
        this.rankType = rankType;
    }

    protected GuildController.GuildEntry.RankRequirement getRequirementsForRank(GuildController.GuildEntry guildEntry, GuildReply.Guild.Rank rank) {
        if (!guildEntry.getRankRequirements().containsKey(rank.getName())) {
            guildEntry.getRankRequirements().put(rank.getName(), GuildController.createEmptyRankRequirement());
        }
        return guildEntry.getRankRequirements().get(rank.getName());
    }

    protected void updateGuildEntry(SkyblockAssistant app, MessageReceivedEvent event, GuildController.GuildEntry guildEntry) {
        try {
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `rank_requirements` = ? WHERE `discord_id` = ?",
                app.getHypixel().getGson().toJson(guildEntry.getRankRequirements()), event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract void handle(SkyblockAssistant app, MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply.Guild.Rank rank, String[] args);

}
