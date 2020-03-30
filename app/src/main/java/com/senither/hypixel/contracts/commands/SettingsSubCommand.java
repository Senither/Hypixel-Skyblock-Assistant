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

package com.senither.hypixel.contracts.commands;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public abstract class SettingsSubCommand extends Command {

    public SettingsSubCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        //
    }

    public void onCommand(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, String[] args) {
        onCommand(event, args);
    }
}
