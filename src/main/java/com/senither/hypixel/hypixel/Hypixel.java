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

package com.senither.hypixel.hypixel;

import com.senither.hypixel.SkyblockAssistant;
import net.hypixel.api.HypixelAPI;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.regex.Pattern;

public class Hypixel {

    private static final Pattern minecraftUsernameRegex = Pattern.compile("^\\w+$", Pattern.CASE_INSENSITIVE);

    private final HypixelAPI hypixelAPI;

    public Hypixel(SkyblockAssistant app) {
        hypixelAPI = new HypixelAPI(UUID.fromString(app.getConfiguration().getHypixelToken()));
    }

    public boolean isValidMinecraftUsername(@Nonnull String username) {
        return username.length() > 2 && username.length() < 17 && minecraftUsernameRegex.matcher(username).find();
    }

    public HypixelAPI getAPI() {
        return hypixelAPI;
    }
}
