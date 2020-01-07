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

package com.senither.hypixel;

import com.senither.hypixel.commands.CommandManager;
import com.senither.hypixel.commands.general.DefaultRoleCommand;
import com.senither.hypixel.commands.general.GuildSetupCommand;
import com.senither.hypixel.commands.general.VerifyCommand;
import com.senither.hypixel.commands.misc.HelpCommand;
import com.senither.hypixel.commands.misc.InformationCommand;
import com.senither.hypixel.commands.misc.PingCommand;
import com.senither.hypixel.commands.statistics.AuctionHouseCommand;
import com.senither.hypixel.commands.statistics.SkillsCommand;
import com.senither.hypixel.commands.statistics.SlayerCommand;
import com.senither.hypixel.database.DatabaseManager;
import com.senither.hypixel.hypixel.Hypixel;
import com.senither.hypixel.listeners.MemberActivityEventListener;
import com.senither.hypixel.listeners.MessageEventListener;
import com.senither.hypixel.scheduler.ScheduleManager;
import com.senither.hypixel.scheduler.jobs.DatabaseCacheCleanupJob;
import com.senither.hypixel.scheduler.jobs.RoleAssignmentJob;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.SessionControllerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class SkyblockAssistant {

    private static final Logger log = LoggerFactory.getLogger(SkyblockAssistant.class);

    private final Configuration configuration;
    private final DatabaseManager databaseManager;
    private final CommandManager commandManager;
    private final ScheduleManager scheduleManager;
    private final Hypixel hypixel;
    private final ShardManager shardManager;

    SkyblockAssistant(Configuration configuration) throws LoginException {
        this.configuration = configuration;

        log.info("Registering commands...");
        this.commandManager = new CommandManager(this);
        commandManager.registerCommand(new DefaultRoleCommand(this));
        commandManager.registerCommand(new GuildSetupCommand(this));
        commandManager.registerCommand(new VerifyCommand(this));
        commandManager.registerCommand(new SkillsCommand(this));
        commandManager.registerCommand(new SlayerCommand(this));
        commandManager.registerCommand(new AuctionHouseCommand(this));
        commandManager.registerCommand(new PingCommand(this));
        commandManager.registerCommand(new InformationCommand(this));
        commandManager.registerCommand(new HelpCommand(this));
        log.info("{} commands have been registered!", commandManager.getCommands().size());

        log.info("Registering jobs...");
        this.scheduleManager = new ScheduleManager(this);
        scheduleManager.registerJob(new DatabaseCacheCleanupJob(this));
        scheduleManager.registerJob(new RoleAssignmentJob(this));
        log.info("{} jobs have been registered!", scheduleManager.entrySet().size());

        log.info("Creating database manager");
        this.databaseManager = new DatabaseManager(this);

        log.info("Creating Hypixel API factory");
        this.hypixel = new Hypixel(this);

        log.info("Opening connection to Discord");
        this.shardManager = buildShardManager();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Hypixel getHypixel() {
        return hypixel;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
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
                new MessageEventListener(this),
                new MemberActivityEventListener(this)
            ).build();
    }
}
