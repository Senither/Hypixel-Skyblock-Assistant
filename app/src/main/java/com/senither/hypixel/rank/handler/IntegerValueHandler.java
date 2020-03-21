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
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.rank.ObjectClosure;
import com.senither.hypixel.contracts.rank.RankCommandHandler;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

public class IntegerValueHandler extends RankCommandHandler {

    private final ObjectClosure<Integer> closure;

    public IntegerValueHandler(ObjectClosure<Integer> closure) {
        this.closure = closure;
    }

    public void handle(
        SkyblockAssistant app,
        MessageReceivedEvent event,
        GuildReply guildReply,
        GuildController.GuildEntry guildEntry,
        GuildReply.Guild.Rank rank,
        String[] args
    ) {
        if (args.length == 0 || !NumberUtil.isNumeric(args[0])) {
            throw new FriendlyException(String.format(
                "The %s value must be a number!",
                rankType.getName()
            ));
        }

        int value = NumberUtil.parseInt(args[0], Integer.MAX_VALUE);

        closure.run(getRequirementsForRank(guildEntry, rank), value);
        updateGuildEntry(app, event, guildReply, guildEntry);

        MessageFactory.makeSuccess(event.getMessage(), "The new :type requirement for **:rank** have successfully been set to **:value**")
            .set("type", rankType.getName())
            .set("rank", rank.getName())
            .set("value", value)
            .queue();
    }
}
