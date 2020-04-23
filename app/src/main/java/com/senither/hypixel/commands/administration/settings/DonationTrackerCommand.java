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

package com.senither.hypixel.commands.administration.settings;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.SettingsSubCommand;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DonationTrackerCommand extends SettingsSubCommand {

    private static final Logger log = LoggerFactory.getLogger(DonationTrackerCommand.class);

    public DonationTrackerCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Donation Tracker System";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to setup or disable the donation tracking system, when enabled,",
            "the system allows staff of a guild, or any with the donation manger Discord role, to",
            "add points to members of the guild which will slowly decay over time, this settings",
            "command are used to setup the time that should elapse between points decaying, and",
            "the amount of points that should decay each time."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <points> <time>` - Sets the donation tracker decay settings.",
            "`:command role <name>` - Setups the donation manager role.",
            "`:command disable` - Disables the donation tracking feature."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 5 24h` - Sets up the tracking system to decrease by 5 points every 24 hours.",
            "`:command role Moderator` - Sets up the donation manager role to use the `Moderator` Discord role."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("donations", "donation");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the donation tracker settings you wish to use as "
                    + "the for the server, or `role` to setup the Discord role that will "
                    + "allow people to add donation points, or `disable` to disable the "
                    + "donation tracker feature."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (args[0].equalsIgnoreCase("role")) {
            setupDonationRole(event, Arrays.copyOfRange(args, 1, args.length));
            return;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            handleDisablingFeature(event);
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "When setting up the donation tracker feature you must include both the amount "
                    + "of points the tracking should decrease points by, and the interval at which "
                    + "the points should decay at."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (!NumberUtil.isNumeric(args[0]) || NumberUtil.parseInt(args[0], 0) <= 0) {
            MessageFactory.makeError(event.getMessage(),
                "The points the donation tracker should decay over time must be a valid number that is greater than 0!"
            ).setTitle("Invalid argument").queue();
            return;
        }
        int pointsToDecay = NumberUtil.parseInt(args[0], 0);

        Integer timeInHours = parseFormattedNumber(args[1]);
        if (timeInHours == null || timeInHours == 0) {
            MessageFactory.makeError(event.getMessage(),
                "The time interval the donation tracker should decay at valid number that is greater than 0, "
                    + "formatted with either an `h` for hours, or `d` for days as a suffix!"
            ).setTitle("Invalid argument").queue();
            return;
        }

        try {
            app.getDatabaseManager().queryUpdate(
                "UPDATE `guilds` SET `donation_time` = ?, `donation_points` = ? WHERE `discord_id` = ?",
                timeInHours, pointsToDecay, event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "The donation tracker have now been setup to decay **:points** points every **:time** hours!")
                .set("points", pointsToDecay)
                .set("time", timeInHours)
                .queue();
        } catch (SQLException e) {
            log.error("Failed to set the donation tracker up for {} to null during reset, error: {}",
                event.getGuild().getId(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Something went wrong while trying to setup the servers donation tracker settings!").queue();
        }
    }

    private void setupDonationRole(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the name of the Discord role you wish to setup as "
                    + "the \"Donate Manager\" role, or enter `disable` to disable "
                    + "the donater role feature."
            ).setTitle("Missing arguments").queue();
            return;
        }

        String roleName = String.join(" ", args).trim();
        if (roleName.equalsIgnoreCase("disable")) {
            try {
                app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `donation_role` = NULL WHERE `discord_id` = ?", event.getGuild().getIdLong());
                GuildController.forgetCacheFor(event.getGuild().getIdLong());

                MessageFactory.makeSuccess(event.getMessage(), "The donator manager role have been disabled successfully!").queue();
            } catch (SQLException e) {
                log.error("Something went wrong while trying to reset the new donator role: {}", e.getMessage(), e);

                MessageFactory.makeError(event.getMessage(),
                    "Something went wrong while trying to save the new donator role value: " + e.getMessage()
                ).queue();
            }
            return;
        }

        List<Role> rolesByName = event.getGuild().getRolesByName(roleName, true);
        if (rolesByName.isEmpty()) {
            MessageFactory.makeWarning(event.getMessage(), "Found one Discord role called **:name**, please use the name of an existing Discord role.")
                .setTitle("Invalid Discord role given")
                .set("name", roleName)
                .queue();
            return;
        }

        Role role = rolesByName.get(0);

        try {
            app.getDatabaseManager().queryUpdate(
                "UPDATE `guilds` SET `donation_role` = ? WHERE `discord_id` = ?",
                role.getIdLong(), event.getGuild().getIdLong()
            );
            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "The donator manager role have been setup to use :role successfully!")
                .set("role", role.getAsMention())
                .queue();
        } catch (SQLException e) {
            log.error("Something went wrong while trying to setup the new donator role: {}", e.getMessage(), e);

            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to save the new donator role value: " + e.getMessage()
            ).queue();
        }
    }

    private Integer parseFormattedNumber(String string) {
        if (string.toLowerCase().endsWith("d")) {
            return NumberUtil.parseInt(string.substring(0, string.length() - 1), 0) * 24;
        }

        if (string.toLowerCase().endsWith("h")) {
            return NumberUtil.parseInt(string.substring(0, string.length() - 1), 0);
        }

        return null;
    }

    private void handleDisablingFeature(MessageReceivedEvent event) {
        try {
            app.getDatabaseManager().queryUpdate(
                "UPDATE `guilds` SET `donation_time` = NULL, `donation_points` = NULL WHERE `discord_id` = ?",
                event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "The donation tracker have now been disabled!").queue();
        } catch (SQLException e) {
            log.error("Failed to set the donation tracker for {} to null during reset, error: {}",
                event.getGuild().getId(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Something went wrong while trying to reset the servers donation tracker settings!").queue();
        }
    }
}
