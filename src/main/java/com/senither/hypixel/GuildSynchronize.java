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

package com.senither.hypixel;

import com.senither.hypixel.listeners.MessageEventListener;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.SessionControllerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class GuildSynchronize {

    private static final Logger log = LoggerFactory.getLogger(GuildSynchronize.class);

    private final Configuration configuration;
    private final ShardManager shardManager;

    GuildSynchronize(Configuration configuration) throws LoginException {
        this.configuration = configuration;
        this.shardManager = buildShardManager();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    private ShardManager buildShardManager() throws LoginException {
        return new DefaultShardManagerBuilder()
            .setSessionController(new SessionControllerAdapter())
            .setToken(configuration.getDiscordToken())
            .setActivity(Activity.watching("the server"))
            .setBulkDeleteSplittingEnabled(false)
            .setContextEnabled(true)
            .addEventListeners(
                new MessageEventListener(this)
            ).build();
    }
}
