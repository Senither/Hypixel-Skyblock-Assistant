/*
 * Copyright (c) 2019.
 *
 * This file is part of Hypixel Guild Synchronizer.
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

package com.senither.hypixel.commands.misc;

import com.senither.hypixel.GuildSynchronize;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class PingCommand extends Command {

    public PingCommand(GuildSynchronize guildSynchronize) {
        super(guildSynchronize);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ping");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        event.getJDA().getRestPing().queue(ping -> {
            event.getChannel().sendMessage(new EmbedBuilder()
                .setColor(MessageType.INFO.getColor())
                .setDescription(String.format(
                    "Pong! Time taken %d ms, websocket heartbeat %d ms.",
                    ping, event.getJDA().getGatewayPing()
                )).build()
            ).queue();
        });
    }
}
