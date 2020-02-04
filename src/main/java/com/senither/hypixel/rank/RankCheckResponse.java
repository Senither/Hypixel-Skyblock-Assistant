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

package com.senither.hypixel.rank;

import net.hypixel.api.reply.GuildReply;

import java.util.HashMap;

public class RankCheckResponse {

    private final GuildReply.Guild.Rank rank;
    private final HashMap<String, Object> metric;

    public RankCheckResponse(GuildReply.Guild.Rank rank, HashMap<String, Object> metric) {
        this.rank = rank;
        this.metric = metric;
    }

    public GuildReply.Guild.Rank getRank() {
        return rank;
    }

    public HashMap<String, Object> getMetric() {
        return metric;
    }
}
