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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.util.function.Function;

public class CustomObjectValueHandler<T> extends RankCommandHandler {

    private final Function<Object, T> getter;
    private final ObjectClosure<T> closure;

    public CustomObjectValueHandler(Function<Object, T> getter, ObjectClosure<T> closure) {
        this.getter = getter;
        this.closure = closure;
    }

    @Override
    public void handle(SkyblockAssistant app, MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply.Guild.Rank rank, String[] args) {
        if (args.length == 0) {
            throw new FriendlyException(String.format(
                "Missing argument for %s requirement",
                rankType.getName()
            ));
        }

        T response = getter.apply(args[0]);
        closure.run(getRequirementsForRank(guildEntry, rank), response);

        updateGuildEntry(app, event, guildEntry);

        MessageFactory.makeSuccess(event.getMessage(),
            "The new :type requirement for **:rank** have successfully been set to **:value**."
        )
            .set("type", rankType.getName())
            .set("rank", rank.getName())
            .set("value", response == null ? "nothing" : response)
            .queue();
    }
}
