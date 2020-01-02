/*
 * Copyright (c) 2019.
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class Command {

    protected final SkyblockAssistant app;
    protected final boolean verificationRequired;

    public Command(SkyblockAssistant app) {
        this(app, true);
    }

    public Command(SkyblockAssistant app, boolean verificationRequired) {
        this.app = app;
        this.verificationRequired = verificationRequired;
    }

    public abstract List<String> getTriggers();

    public abstract void onCommand(MessageReceivedEvent event, String[] args);

    public final boolean isVerificationRequired() {
        return verificationRequired;
    }
}
