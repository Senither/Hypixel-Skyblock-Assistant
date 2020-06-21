package com.senither.hypixel.commands.administration.settings;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.SettingsSubCommand;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SplashTrackerCommand extends SettingsSubCommand {

    private static final Logger log = LoggerFactory.getLogger(SplashTrackerCommand.class);

    public SplashTrackerCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Splash Tracker System";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to setup or disable the splash tracking system, when enabled,",
            "the system allows anyone in the guild to queue splashes that will then notify everyone",
            "in the guild 5 minutes before they're suppose to happen, there are also splash",
            "leaderboards, and ways to see how many splashes people have done in the guild",
            "in their lifetime, as-well-as average splashes per day.",
            "People with the splash manager role will be able to edit, cancel, and remove other peoples splashes."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <channel> <role>` - Enables the splash tracker.",
            "`:command points <status>` - Toggles splash points on or off.",
            "`:command disable` - Disables the splash tracker."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command #guild-splashes @Splash Manager` - Enables the splash tracker with the given settings.",
            "`:command points on` - Enables giving donation points to splashes when they splash.",
            "`:command disable` - Disables the splash tracker."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("splash", "splashes");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the splash tracker settings you wish to use as "
                    + "the for the server, or `disable` to disable the "
                    + "splash tracker feature."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            handleDisablingFeature(event);
            return;
        }

        if (args[0].equalsIgnoreCase("points") || args[0].equalsIgnoreCase("point")) {
            handlePoints(event, Arrays.copyOfRange(args, 1, args.length));
            return;
        }

        List<TextChannel> channelsByName = event.getGuild().getTextChannelsByName(args[0], true);
        if (channelsByName.isEmpty()) {
            MessageFactory.makeError(event.getMessage(),
                "Invalid channel name given, the channel must be an existing text channel that the bot has permissions to send messages in!"
            ).queue();
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "Missing splash role, the splash manager role is required!"
            ).queue();
            return;
        }

        List<Role> rolesByName = event.getGuild().getRolesByName(args[1], true);
        if (rolesByName.isEmpty()) {
            MessageFactory.makeError(event.getMessage(),
                "Invalid role name given, you must provide a valid name of an existing role to setup the splash manager role!"
            ).queue();
            return;
        }

        try {
            app.getDatabaseManager().queryUpdate(
                "UPDATE `guilds` SET\n" +
                    "    `splash_channel` = ?,\n" +
                    "    `splash_role` = ?\n" +
                    "WHERE `discord_id` = ?",
                channelsByName.get(0).getIdLong(),
                rolesByName.get(0).getIdLong(),
                event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "Splash is setup to use :channel and :role")
                .set("channel", channelsByName.get(0).getAsMention())
                .set("role", rolesByName.get(0).getAsMention())
                .queue();
        } catch (SQLException e) {
            log.error("Something went wrong while trying to reset the new splash settings: {}", e.getMessage(), e);

            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to save the new splash settings: " + e.getMessage()
            ).queue();
        }
    }

    private void handlePoints(MessageReceivedEvent event, String[] args) {
        GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
        if (guildEntry == null) {
            MessageFactory.makeError(event.getMessage(),
                "Failed to load the guild splash settings from the database, please try again later!"
            ).queue();
            return;
        }

        if (args.length == 0) {
            MessageFactory.makeInfo(event.getMessage(),
                "The splash points system is currently **:status**!"
            ).set("status", guildEntry.getSplashPoints() ? "enabled" : "disabled").queue();
            return;
        }

        boolean status = Arrays.asList("on", "enable", "enabled", "yes", "y").contains(args[0].toLowerCase());

        try {
            app.getDatabaseManager().queryUpdate(
                "UPDATE `guilds` SET `splash_points` = ? WHERE `discord_id` = ?",
                status, event.getGuild().getIdLong()
            );

            MessageFactory.makeSuccess(event.getMessage(),
                "The splash points feature have now been **:status**!"
            ).set("status", status ? "enabled" : "disabled").queue();

            GuildController.forgetCacheFor(event.getGuild().getIdLong());
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "Failed to save the guild settings, error: " + e.getMessage()
            ).queue();
        }
    }

    private void handleDisablingFeature(MessageReceivedEvent event) {
        try {
            app.getDatabaseManager().queryUpdate(
                "UPDATE `guilds` SET\n" +
                    "    `splash_channel` = NULL,\n" +
                    "    `splash_role` = NULL,\n" +
                    "WHERE `discord_id` = ?",
                event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "The splash tracker have now been disabled!").queue();
        } catch (SQLException e) {
            log.error("Failed to set the splash tracker for {} to null during reset, error: {}",
                event.getGuild().getId(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Something went wrong while trying to reset the servers splash tracker settings!").queue();
        }
    }
}
