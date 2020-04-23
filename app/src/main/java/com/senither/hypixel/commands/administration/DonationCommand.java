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

package com.senither.hypixel.commands.administration;

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.database.controller.PlayerDonationController;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.sql.SQLException;
import java.util.*;

public class DonationCommand extends Command {

    public DonationCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Donation Tracker";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to give donation points to people, the points will",
            "slowly go down over time, allowing guild staff to view who has donated the",
            "most recently. This is useful for tracking splash donations, or anything",
            "else that the guild might take donations for."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list [page]` - Lists every player in the guild, and their donation points",
            "`:command add <player> <points>` - Gives the player X amount of points"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command list 2` - Views page 2 of the donation leaderboard",
            "`:command add Senither 5` - Gives Senither 5 donation points"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("donation", "donations");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
        if (guildEntry == null) {
            MessageFactory.makeError(event.getMessage(),
                "The server is not currently setup with a guild, you must setup "
                    + "the server with a guild before you can use this command!"
            ).setTitle("Server is not setup").queue();
            return;
        }

        if (!guildEntry.isDonationsTrackerEnabled()) {
            MessageFactory.makeError(event.getMessage(),
                "The donation tracker feature have not yet been enabled for the server, you "
                    + "must setup the feature before being able to use this command, you can enable the "
                    + "feature by running:"
                    + "\n```h!settings donation 5 1d```"
            ).setTitle("Donation tracker is not setup").queue();
            return;
        }

        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must specify the action you wish to preform for the donation points."
            ).setTitle("Missing argument").queue();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "ls":
            case "list":
            case "leaderboard":
                showLeaderboard(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "add":
            case "give:":
            case "reward":
                if (!isGuildMasterOrOfficerOfServerGuild(event, guildEntry)) {
                    MessageFactory.makeError(event.getMessage(),
                        "You must be the guild master or an officer of the **:name** guild to add donation points to people!"
                    ).set("name", guildEntry.getName()).setTitle("Missing permissions").queue();
                    return;
                }
                addPointsToPlayer(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            default:
                MessageFactory.makeError(event.getMessage(),
                    "Invalid donation points action provided, please use either `list` to view the donation points leaderboard, or `add` to add points to a player."
                ).setTitle("Invalid Argument").queue();
        }
    }

    private void showLeaderboard(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);

        List<UUID> memberUuids = new ArrayList<>();
        guildReply.getGuild().getMembers().forEach(member -> memberUuids.add(member.getUuid()));

        StringBuilder stringifiedParams = new StringBuilder();
        for (UUID ignored : memberUuids) {
            stringifiedParams.append("?, ");
        }

        try {
            Collection result = app.getDatabaseManager().query(String.format(
                "SELECT `uuid`, `username` FROM `uuids` WHERE `uuid` IN (%s)",
                stringifiedParams.toString().substring(0, stringifiedParams.length() - 2)
            ), memberUuids.toArray());

            HashMap<UUID, String> memberUsernameMap = new HashMap<>();
            for (DataRow row : result) {
                memberUsernameMap.put(UUID.fromString(row.getString("uuid")), row.getString("username"));
            }

            int rank = 1;
            List<String> messageRows = new ArrayList<>();
            for (PlayerDonationController.PlayerDonationEntry player : PlayerDonationController.getPlayersById(app.getDatabaseManager(), event.getGuild().getIdLong())) {
                if (!memberUsernameMap.containsKey(player.getUuid())) {
                    continue;
                }
                messageRows.add(String.format("#%s: %s > %s",
                    padString(String.valueOf(rank++), 4),
                    padString(memberUsernameMap.get(player.getUuid()), 16),
                    player.getPoints()
                ));
                memberUsernameMap.remove(player.getUuid());
            }

            for (String username : memberUsernameMap.values()) {
                messageRows.add(String.format("#%s: %s > 0",
                    padString(String.valueOf(rank++), 4),
                    padString(username, 16)
                ));
            }

            List<String> message = new ArrayList<>();
            SimplePaginator<String> paginator = new SimplePaginator<>(
                messageRows, 25, args.length == 0 ? 1 : NumberUtil.parseInt(args[0], 0)
            );
            paginator.forEach((index, key, val) -> message.add(val));

            MessageFactory.makeInfo(event.getMessage(), String.format("Donation points currently decrease by **:points** points every **:time** hours!```ada\n%s```\n%s",
                String.join("\n", message), paginator.generateFooter(Constants.COMMAND_PREFIX + getTriggers().get(0) + " list")
            ))
                .setTitle("Donation Points Leaderboard")
                .set("points", guildEntry.getDonationPoints())
                .set("time", guildEntry.getDonationTime())
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addPointsToPlayer(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the username of the player you want to add points to."
            ).setTitle("Missing username").queue();
            return;
        }

        if (args.length == 1 || !NumberUtil.isNumeric(args[1])) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the amount of points you wish to give to :name"
            ).set("name", args[0]).setTitle("Missing donation points").queue();
            return;
        }

        UUID uuid;
        String username = args[0];
        int points = NumberUtil.parseInt(args[1], 0);

        try {
            uuid = app.getHypixel().getUUIDFromName(username);
        } catch (SQLException e) {
            throw new FriendlyException("Failed to find a UUID matching the given username!");
        }

        if (uuid == null) {
            MessageFactory.makeError(event.getMessage(),
                "The given username is not a valid or existing Minecraft username."
            ).setTitle("Invalid username provided").queue();
            return;
        }

        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);
        boolean isMember = guildReply.getGuild().getMembers().stream().filter(member -> member.getUuid().equals(uuid)).count() > 0;

        if (!isMember) {
            MessageFactory.makeError(event.getMessage(),
                ":name is not a member of the guild, and can therefore not be given any donation points."
            ).set("name", args[0]).setTitle("User is not a guild member").queue();
            return;
        }

        PlayerDonationController.PlayerDonationEntry donationEntry = PlayerDonationController.getPlayerByUuid(app.getDatabaseManager(), event.getGuild().getIdLong(), uuid);
        if (donationEntry == null) {
            MessageFactory.makeError(event.getMessage(),
                "Failed to load the players donation points entry, please try again later."
            ).setTitle("Failed to load donation entry").queue();
            return;
        }

        donationEntry.setPoints(donationEntry.getPoints() + points);

        try {
            app.getDatabaseManager().queryUpdate("UPDATE `donation_points` SET `points` = ? WHERE `discord_id` = ? AND `uuid` = ?",
                donationEntry.getPoints(), donationEntry.getDiscordId(), donationEntry.getUuid()
            );

            MessageFactory.makeSuccess(event.getMessage(), "**:points** donation points have been given to **:name**")
                .set("points", points)
                .set("name", args[0])
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();

            MessageFactory.makeError(event.getMessage(), "Failed to store the donation points due to an error: " + e.getMessage()).queue();
        }
    }

    private String padString(String string, int size) {
        StringBuilder stringBuilder = new StringBuilder(string);
        while (stringBuilder.length() < size) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
