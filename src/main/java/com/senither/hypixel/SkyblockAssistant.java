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
import com.senither.hypixel.commands.administration.*;
import com.senither.hypixel.commands.general.HelpCommand;
import com.senither.hypixel.commands.general.VerifyCommand;
import com.senither.hypixel.commands.misc.BotStatsCommand;
import com.senither.hypixel.commands.misc.InformationCommand;
import com.senither.hypixel.commands.misc.PingCommand;
import com.senither.hypixel.commands.statistics.*;
import com.senither.hypixel.config.Configuration;
import com.senither.hypixel.config.ConfigurationLoader;
import com.senither.hypixel.database.DatabaseManager;
import com.senither.hypixel.hypixel.Hypixel;
import com.senither.hypixel.listeners.MemberActivityEventListener;
import com.senither.hypixel.listeners.MessageEventListener;
import com.senither.hypixel.reports.ReportService;
import com.senither.hypixel.scheduler.ScheduleManager;
import com.senither.hypixel.scheduler.jobs.DrainReportQueueJob;
import com.senither.hypixel.scheduler.jobs.HypixelRankSynchronizeJob;
import com.senither.hypixel.scheduler.jobs.RoleAssignmentJob;
import com.senither.hypixel.servlet.WebServlet;
import com.senither.hypixel.servlet.routes.HelloRoute;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.SessionControllerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class SkyblockAssistant {

    private static final Logger log = LoggerFactory.getLogger(SkyblockAssistant.class);

    private final Configuration configuration;
    private final DatabaseManager databaseManager;
    private final CommandManager commandManager;
    private final ScheduleManager scheduleManager;
    private final WebServlet servlet;
    private final Hypixel hypixel;
    private final ShardManager shardManager;

    SkyblockAssistant() throws LoginException, IOException {
        log.debug("====================================================");
        log.debug("Starting the application with debug logging enabled!");
        log.debug("Started at {}", Constants.STARTED_BOT_AT.toDayDateTimeString());
        log.debug("====================================================\n");

        this.configuration = new ConfigurationLoader(this).get();

        log.info("Registering commands...");
        this.commandManager = new CommandManager(this);
        commandManager.registerCommand(new AutoRenameCommand(this));
        commandManager.registerCommand(new DefaultRoleCommand(this));
        commandManager.registerCommand(new GuildRankScanCommand(this));
        commandManager.registerCommand(new GuildSetupCommand(this));
        commandManager.registerCommand(new RankCheckCommand(this));
        commandManager.registerCommand(new RankRequirementCommand(this));
        commandManager.registerCommand(new VerifyCommand(this));
        commandManager.registerCommand(new SkillsCommand(this));
        commandManager.registerCommand(new SlayerCommand(this));
        commandManager.registerCommand(new AuctionHouseCommand(this));
        commandManager.registerCommand(new BankBalanceCommand(this));
        commandManager.registerCommand(new GuildExperienceLeaderboardCommand(this));
        commandManager.registerCommand(new PetsCommand(this));
        commandManager.registerCommand(new PlayerOverviewCommand(this));
        commandManager.registerCommand(new TalismansCommand(this));
        commandManager.registerCommand(new PingCommand(this));
        commandManager.registerCommand(new BotStatsCommand(this));
        commandManager.registerCommand(new InformationCommand(this));
        commandManager.registerCommand(new HelpCommand(this));
        log.info("{} commands have been registered!", commandManager.getCommands().size());

        log.info("Registering jobs...");
        this.scheduleManager = new ScheduleManager(this);
        scheduleManager.registerJob(new RoleAssignmentJob(this));
        scheduleManager.registerJob(new DrainReportQueueJob(this));
        scheduleManager.registerJob(new HypixelRankSynchronizeJob(this));
        log.info("{} jobs have been registered!", scheduleManager.entrySet().size());

        log.info("Creating database manager");
        this.databaseManager = new DatabaseManager(this);

        log.info("Creating Hypixel API factory");
        this.hypixel = new Hypixel(this);

        log.info("Resuming unfinished reports");
        ReportService.resumeUnfinishedReports(this);

        if (configuration.getServlet().isEnabled()) {
            log.info("Creating web servlet on port {}", configuration.getServlet().getPort());
            this.servlet = new WebServlet(configuration.getServlet().getPort());
            servlet.registerGet("hello", new HelloRoute());
        } else {
            this.servlet = null;
        }

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

    public void shutdown(int code) {
        log.info("Shutting down process with code {}", code);
        System.exit(code);
    }
}
