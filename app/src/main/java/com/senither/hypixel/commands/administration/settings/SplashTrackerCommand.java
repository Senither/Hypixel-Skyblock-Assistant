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
        return Arrays.asList("TODO");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList("TODO");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList("TODO");
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

        List<TextChannel> channelsByName = event.getGuild().getTextChannelsByName(args[0], true);
        if (channelsByName.isEmpty()) {
            MessageFactory.makeError(event.getMessage(), "Invalid channel name given, TODO:").queue();
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "Missing splash role, TODO"
            ).queue();
            return;
        }

        List<Role> rolesByName = event.getGuild().getRolesByName(args[1], true);
        if (rolesByName.isEmpty()) {
            MessageFactory.makeError(event.getMessage(), "Invalid role name given, TODO:").queue();
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
