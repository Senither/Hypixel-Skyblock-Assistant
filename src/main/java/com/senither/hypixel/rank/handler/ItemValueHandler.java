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

package com.senither.hypixel.rank.handler;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.rank.ObjectClosure;
import com.senither.hypixel.contracts.rank.RankCommandHandler;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.util.HashMap;
import java.util.function.Function;

public class ItemValueHandler extends RankCommandHandler {

    private final ObjectClosure<Integer> closure;
    private final Function<GuildController.GuildEntry.RankRequirement, HashMap<String, Integer>> getter;

    public ItemValueHandler(ObjectClosure<Integer> closure, Function<GuildController.GuildEntry.RankRequirement, HashMap<String, Integer>> getter) {
        this.closure = closure;
        this.getter = getter;
    }

    @Override
    public void handle(SkyblockAssistant app, MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply.Guild.Rank rank, String[] args) {

    }
}
