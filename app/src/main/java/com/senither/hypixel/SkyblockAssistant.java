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

import com.senither.hypixel.blacklist.Blacklist;
import com.senither.hypixel.commands.CommandManager;
import com.senither.hypixel.commands.administration.*;
import com.senither.hypixel.commands.calculators.PetsCalculatorCommand;
import com.senither.hypixel.commands.calculators.SkillsCalculatorCommand;
import com.senither.hypixel.commands.calculators.SkillsExperienceCalculatorCommand;
import com.senither.hypixel.commands.calculators.WeightCalculatorCommand;
import com.senither.hypixel.commands.general.HelpCommand;
import com.senither.hypixel.commands.general.VerifyCommand;
import com.senither.hypixel.commands.misc.*;
import com.senither.hypixel.commands.statistics.*;
import com.senither.hypixel.config.Configuration;
import com.senither.hypixel.config.ConfigurationLoader;
import com.senither.hypixel.database.DatabaseManager;
import com.senither.hypixel.hypixel.Hypixel;
import com.senither.hypixel.listeners.GenericEventListener;
import com.senither.hypixel.listeners.MemberActivityEventListener;
import com.senither.hypixel.listeners.MessageEventListener;
import com.senither.hypixel.listeners.ReactionEventListener;
import com.senither.hypixel.metrics.Metrics;
import com.senither.hypixel.reports.ReportService;
import com.senither.hypixel.scheduler.ScheduleManager;
import com.senither.hypixel.scheduler.jobs.*;
import com.senither.hypixel.servlet.WebServlet;
import com.senither.hypixel.servlet.routes.*;
import com.senither.hypixel.splash.SplashManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
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
    private final SplashManager splashManager;
    private final ScheduleManager scheduleManager;
    private final Blacklist blacklist;
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
        commandManager.registerCommand(new BanLogCommand(this));
        commandManager.registerCommand(new DonationCommand(this));
        commandManager.registerCommand(new GuildRankScanCommand(this));
        commandManager.registerCommand(new GuildSetupCommand(this));
        commandManager.registerCommand(new MessageCommand(this));
        commandManager.registerCommand(new RankCheckCommand(this));
        commandManager.registerCommand(new RankRequirementCommand(this));
        commandManager.registerCommand(new SettingsCommand(this));
        commandManager.registerCommand(new SplashCommand(this));
        commandManager.registerCommand(new PetsCalculatorCommand(this));
        commandManager.registerCommand(new SkillsCalculatorCommand(this));
        commandManager.registerCommand(new SkillsExperienceCalculatorCommand(this));
        commandManager.registerCommand(new WeightCalculatorCommand(this));
        commandManager.registerCommand(new VerifyCommand(this));
        commandManager.registerCommand(new SkillsCommand(this));
        commandManager.registerCommand(new SlayerCommand(this));
        commandManager.registerCommand(new SplashCostCommand(this));
        commandManager.registerCommand(new AuctionHouseCommand(this));
        commandManager.registerCommand(new AuctionHouseStatisticsCommand(this));
        commandManager.registerCommand(new BankBalanceCommand(this));
        commandManager.registerCommand(new BazaarCommand(this));
        commandManager.registerCommand(new CatacombsCommand(this));
        commandManager.registerCommand(new GuildExperienceLeaderboardCommand(this));
        commandManager.registerCommand(new LeaderboardCommand(this));
        commandManager.registerCommand(new PetsCommand(this));
        commandManager.registerCommand(new PlayerLeaderboardCommand(this));
        commandManager.registerCommand(new PlayerOverviewCommand(this));
        commandManager.registerCommand(new TalismansCommand(this));
        commandManager.registerCommand(new PingCommand(this));
        commandManager.registerCommand(new BoopCommand(this));
        commandManager.registerCommand(new BoopOptCommand(this));
        commandManager.registerCommand(new BotStatsCommand(this));
        commandManager.registerCommand(new InformationCommand(this));
        commandManager.registerCommand(new HelpCommand(this));
        log.info("{} commands have been registered!", commandManager.getCommands().size());

        log.info("Registering jobs...");
        this.scheduleManager = new ScheduleManager(this);
        scheduleManager.registerJob(new SyncMetricsJob(this));
        scheduleManager.registerJob(new SplashQueueJob(this));
        scheduleManager.registerJob(new UpdateGuildDataJob(this));
        scheduleManager.registerJob(new GarbageCollectorJob(this));
        scheduleManager.registerJob(new DrainReportQueueJob(this));
        scheduleManager.registerJob(new DecayDonationPointsJob(this));
        scheduleManager.registerJob(new HypixelRankSynchronizeJob(this));
        log.info("{} jobs have been registered!", scheduleManager.entrySet().size());

        log.info("Creating database manager");
        this.databaseManager = new DatabaseManager(this);

        log.info("Preparing blacklist and syncing the list with the database");
        blacklist = new Blacklist(this);
        blacklist.syncBlacklistWithDatabase();

        log.info("Creating Hypixel API factory");
        this.hypixel = new Hypixel(this);
        if (!this.hypixel.isLeaderboardApiValid()) {
            log.warn("Invalid leaderboard URI provided, falling back to the public leaderboard URI");
            configuration.setLeaderboardUri(null);
        }

        log.info("Creating splash manager & resuming splash tracking");
        this.splashManager = new SplashManager(this);

        log.info("Resuming unfinished reports");
        ReportService.resumeUnfinishedReports(this);

        if (configuration.getServlet().isEnabled()) {
            Metrics.setup();

            log.info("Creating web servlet on port {}", configuration.getServlet().getPort());
            this.servlet = new WebServlet(configuration.getServlet().getPort());
            servlet.registerGet("/metrics", new GetMetrics(this));
            servlet.registerGet("report/:id", new GetReportRoute(this));
            servlet.registerGet("username", new GetUsernameRoute(this));

            if (configuration.getServlet().getAccessToken() != null) {
                servlet.registerGet("player/:username", new GetProfileRoute(this));
                servlet.registerGet("guild/:name", new GetGuildRoute(this));
            }
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

    public SplashManager getSplashManager() {
        return splashManager;
    }

    public Blacklist getBlacklist() {
        return blacklist;
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
        return DefaultShardManagerBuilder.createDefault(configuration.getDiscordToken())
            .setSessionController(new SessionControllerAdapter())
            .setActivity(Activity.watching("the server"))
            .setMemberCachePolicy(MemberCachePolicy.NONE)
            .setBulkDeleteSplittingEnabled(false)
            .setEnableShutdownHook(true)
            .setContextEnabled(true)
            .setAutoReconnect(true)
            .addEventListeners(
                new ReactionEventListener(),
                new GenericEventListener(),
                new MessageEventListener(this),
                new MemberActivityEventListener(this)
            ).build();
    }

    public void shutdown(int code) {
        log.info("Shutting down process with code {}", code);
        System.exit(code);
    }
}
