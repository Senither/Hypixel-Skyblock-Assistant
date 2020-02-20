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

public class DoubleObjectValueHandler extends RankCommandHandler {

    private final ObjectClosure<Integer> closureFirst;
    private final ObjectClosure<Integer> closureSecond;
    private final String message;

    public DoubleObjectValueHandler(ObjectClosure<Integer> closureFirst, ObjectClosure<Integer> closureSecond, String message) {
        this.closureFirst = closureFirst;
        this.closureSecond = closureSecond;
        this.message = message;
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
                "The first %s value must be a number!",
                rankType.getName()
            ));
        }

        if (args.length == 1 || !NumberUtil.isNumeric(args[1])) {
            throw new FriendlyException(String.format(
                "The second %s value must be a number!",
                rankType.getName()
            ));
        }

        int first = NumberUtil.parseInt(args[0], Integer.MAX_VALUE);
        int second = NumberUtil.parseInt(args[1], Integer.MAX_VALUE);

        closureFirst.run(getRequirementsForRank(guildEntry, rank), first);
        closureSecond.run(getRequirementsForRank(guildEntry, rank), second);

        updateGuildEntry(app, event, guildReply, guildEntry);

        MessageFactory.makeSuccess(event.getMessage(), message == null
            ? "The new :type requirement for **:rank** have successfully been set to **:first** and **:second**."
            : message
        )
            .set("type", rankType.getName())
            .set("rank", rank.getName())
            .set("first", first)
            .set("second", second)
            .queue();
    }
}
