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
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.commands.DonationAdditionFunction;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.database.controller.PlayerDonationController;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DonationCommand extends Command {

    public static final HashMap<Long, DonationAdditionFunction> confirmationQueue = new HashMap<>();

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
            "`:command [page]` - Lists every player in the guild, and their donation points",
            "`:command add <player> <points> [message]` - Gives the player X amount of points",
            "`:command show <player>` - Show the amount of points the given user has"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 2` - Views page 2 of the donation leaderboard",
            "`:command add Senither 5` - Gives Senither 5 donation points",
            "`:command add Senither 10 Some cool stuff` - Gives Senither 10 donation points with the note of \"Some coll stuff\"",
            "`:command show Senither` - Shows how many donation points Senither has"
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
            showLeaderboard(guildEntry, event, new String[0]);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "ls":
            case "list":
            case "leaderboard":
                showLeaderboard(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "show":
            case "lookup":
                showPlayerInfo(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "add":
            case "give:":
            case "reward":
                if (!hasPermissionToAddPoints(event, guildEntry)) {
                    Role donationRole = event.getGuild().getRoleById(guildEntry.getDonationRole());
                    MessageFactory.makeError(event.getMessage(), donationRole == null
                        ? "You must be the guild master or an officer of the **:name** guild to add donation points to people!"
                        : "You must be the guild master or an officer of the **:name** guild, or have the :role Discord role to add donation points to people!"
                    )
                        .set("name", guildEntry.getName())
                        .set("role", donationRole == null ? "" : donationRole.getAsMention())
                        .setTitle("Missing permissions")
                        .queue();
                    return;
                }
                addPointsToPlayer(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            default:
                if (NumberUtil.isNumeric(args[0])) {
                    showLeaderboard(guildEntry, event, args);
                    break;
                }
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

            UUID userUUID = null;
            try {
                userUUID = app.getHypixel().getUUIDFromUser(event.getAuthor());
            } catch (SQLException ignored) {
            }

            UUID finalUserUUID = userUUID;
            boolean isGuildMember = guildReply.getGuild().getMembers().stream()
                .filter(member -> member.getUuid().equals(finalUserUUID))
                .count() > 0;

            int rank = 1;
            int position = -1;
            PlayerDonationController.PlayerDonationEntry currentPlayer = null;

            Map<UUID, Integer> splashes = new HashMap<>();
            if (guildEntry.isSplashTrackerEnabled()) {
                Collection splashResults = app.getDatabaseManager().query(
                    "SELECT `uuid`, COUNT(`id`) as 'total' FROM `splashes` WHERE `discord_id` = ? AND `splash_at` > ? GROUP BY `uuid`",
                    event.getGuild().getIdLong(),
                    Carbon.now().subHours(guildEntry.getDonationTime())
                );

                for (DataRow splashResult : splashResults) {
                    splashes.put(
                        UUID.fromString(splashResult.getString("uuid")),
                        splashResult.getInt("total")
                    );
                }
            }

            List<String> messageRows = new ArrayList<>();
            for (PlayerDonationController.PlayerDonationEntry player : PlayerDonationController.getPlayersById(app.getDatabaseManager(), event.getGuild().getIdLong())) {
                if (!memberUsernameMap.containsKey(player.getUuid())) {
                    continue;
                }

                if (player.getUuid().equals(userUUID)) {
                    position = rank;
                    currentPlayer = player;
                }

                String splashMessage = "";
                if (splashes.containsKey(player.getUuid())) {
                    Integer splashCounter = splashes.get(player.getUuid());
                    splashMessage = String.format(" < %s %s",
                        splashCounter, splashCounter == 1 ? "Splash" : "Splashes"
                    );
                }

                messageRows.add(String.format("#%s: %s > %s%s\n-----: Last donated %s",
                    padString(String.valueOf(rank++), 4),
                    padString(memberUsernameMap.get(player.getUuid()), 16),
                    player.getPoints(),
                    splashMessage,
                    player.getLastDonatedAt().diffForHumans()
                ));
                memberUsernameMap.remove(player.getUuid());
            }

            for (Map.Entry<UUID, String> memberMapEntry : memberUsernameMap.entrySet()) {
                String splashMessage = "";
                if (splashes.containsKey(memberMapEntry.getKey())) {
                    Integer splashCounter = splashes.get(memberMapEntry.getKey());
                    splashMessage = String.format(" < %s %s",
                        splashCounter, splashCounter == 1 ? "Splash" : "Splashes"
                    );
                }

                messageRows.add(String.format("#%s: %s > 0%s\n-----: Has never donated",
                    padString(String.valueOf(rank++), 4),
                    padString(memberMapEntry.getValue(), 16),
                    splashMessage
                ));
            }

            List<String> message = new ArrayList<>();
            SimplePaginator<String> paginator = new SimplePaginator<>(
                messageRows, 10, args.length == 0 ? 1 : NumberUtil.parseInt(args[0], 0)
            );
            paginator.forEach((index, key, val) -> message.add(val));

            String note = "";
            if (isGuildMember) {
                note = position < 0
                    ? "> You're currently unranked with **0** points as you have yet to donate anything.\n\n"
                    : String.format("> You're ranked **#%s** in the guild with **%s** points! Points will be deducted\n> from you in approximately %s.\n\n",
                    position, currentPlayer.getPoints(), currentPlayer.getLastCheckedAt().addHours(guildEntry.getDonationTime()).diffForHumans()
                );
            }

            MessageFactory.makeInfo(event.getMessage(), String.format(
                "Donation points currently decrease by **:points** points every **:time** hours!%s```ada\n%s```\n%s%s",
                guildEntry.isSplashTrackerEnabled()
                    ? "\nSplash tracking is enabled, so people who have splashed in the last **:time** hours will also be displayed on the leaderboard." : "",
                String.join("\n", message), note, paginator.generateFooter(Constants.COMMAND_PREFIX + getTriggers().get(0))
            ))
                .setTitle("Donation Points Leaderboard")
                .set("points", guildEntry.getDonationPoints())
                .set("time", guildEntry.getDonationTime())
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showPlayerInfo(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the username of the player you show donation points for."
            ).setTitle("Missing username").queue();
            return;
        }

        UUID uuid;
        String username = args[0];

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
        boolean isMember = guildReply.getGuild().getMembers().stream().anyMatch(member -> member.getUuid().equals(uuid));

        if (!isMember) {
            MessageFactory.makeError(event.getMessage(),
                ":name is not a member of the guild, and does therefore not have any donation points."
            ).set("name", formatPlayerUsername(args[0])).setTitle("User is not a guild member").queue();
            return;
        }

        PlayerDonationController.PlayerDonationEntry player = PlayerDonationController.getPlayerByUuid(
            app.getDatabaseManager(), event.getGuild().getIdLong(), uuid, false
        );

        try {
            username = app.getHypixel().getUsernameFromUuid(uuid);
        } catch (SQLException ignored) {
        }

        PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(), "")
            .set("name", formatPlayerUsername(username))
            .set("points", player != null ? NumberUtil.formatNicely(player.getPoints()) : 0)
            .set("time", player != null ? player.getLastDonatedAt().diffForHumans() : "Unknown")
            .setTitle(String.format("%s Donation Points", username));

        if (player == null) {
            message.setDescription("**:name** has never donated before, and thus has no donation points.").queue();
        } else {
            message.setDescription("**:name** has **:points** points, they last donated **:time** ago.").queue();
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
            ).set("name", formatPlayerUsername(args[0])).setTitle("User is not a guild member").queue();
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        DonationAdditionFunction function = createDonationNAdditionFunction(event, guildEntry, username, uuid, points, message);
        if (!message.isEmpty()) {
            function.handle();
            return;
        }

        MessageFactory.makeWarning(event.getMessage(), String.join("\n", Arrays.asList(
            "You're about to send add **:points** points to **:username** without a note!",
            "Are you sure you want to continue?"
        ))).setFooter("This message will automatically be deleted in 60 seconds")
            .set("points", points)
            .set("username", username)
            .queue(confirmMessage -> {
                confirmationQueue.put(confirmMessage.getIdLong(), function);

                confirmMessage.addReaction("✅").queue();
                confirmMessage.delete().queueAfter(60, TimeUnit.SECONDS, ignored -> {
                    confirmationQueue.remove(confirmMessage.getIdLong());
                }, throwable -> {
                    // Ignored
                });
            });
    }

    private DonationAdditionFunction createDonationNAdditionFunction(
        MessageReceivedEvent event,
        GuildController.GuildEntry guildEntry,
        String username,
        UUID uuid,
        int points,
        String message
    ) {
        return new DonationAdditionFunction() {
            @Override
            public void handle() {
                PlayerDonationController.PlayerDonationEntry donationEntry = PlayerDonationController.getPlayerByUuid(app.getDatabaseManager(), event.getGuild().getIdLong(), uuid);
                if (donationEntry == null) {
                    MessageFactory.makeError(event.getMessage(),
                        "Failed to load the players donation points entry, please try again later."
                    ).setTitle("Failed to load donation entry").queue();
                    return;
                }

                donationEntry.setPoints(donationEntry.getPoints() + points);

                try {
                    app.getDatabaseManager().queryUpdate("UPDATE `donation_points` SET `points` = ?, `last_donated_at` = ? WHERE `discord_id` = ? AND `uuid` = ?",
                        donationEntry.getPoints(), Carbon.now(), donationEntry.getDiscordId(), donationEntry.getUuid()
                    );

                    MessageFactory.makeSuccess(event.getMessage(), "**:points** donation points have been given to **:name**")
                        .set("points", points)
                        .set("name", formatPlayerUsername(username))
                        .queue();

                    if (guildEntry.getDonationChannel() == null || guildEntry.getDonationChannel() == 0) {
                        return;
                    }

                    TextChannel logChannel = event.getGuild().getTextChannelById(guildEntry.getDonationChannel());
                    if (logChannel == null) {
                        return;
                    }

                    MessageFactory.makeEmbeddedMessage(logChannel, MessageType.INFO.getColor(), message.isEmpty()
                        ? "**:user** was given **:points** points by **:author**, no note were given!"
                        : "**:user** was given **:points** points by **:author** for \":note\""
                    ).setFooter("The points were given by " + event.getAuthor().getAsTag() + " (ID: " + event.getAuthor().getId() + ")")
                        .set("user", formatPlayerUsername(app.getHypixel().getUsernameFromUuid(uuid)))
                        .set("author", formatPlayerUsername(getUsernameFromUser(event.getAuthor())))
                        .set("points", points)
                        .set("note", message)
                        .queue();
                } catch (SQLException e) {
                    e.printStackTrace();

                    MessageFactory.makeError(event.getMessage(), "Failed to store the donation points due to an error: " + e.getMessage()).queue();
                }
            }

            @Override
            public long getAuthorId() {
                return event.getAuthor().getIdLong();
            }
        };
    }

    private boolean hasPermissionToAddPoints(MessageReceivedEvent event, GuildController.GuildEntry guildEntry) {
        if (isGuildMasterOrOfficerOfServerGuild(event, guildEntry)) {
            return true;
        }

        Long donationRole = guildEntry.getDonationRole();
        if (donationRole == null || donationRole == 0L || event.getMember() == null) {
            return false;
        }

        for (Role memberRole : event.getMember().getRoles()) {
            if (memberRole.getIdLong() == donationRole) {
                return true;
            }
        }

        return false;
    }

    private String formatPlayerUsername(String username) {
        if (!username.startsWith("_")) {
            return username;
        }
        return "\\" + username;
    }

    private String padString(String string, int size) {
        StringBuilder stringBuilder = new StringBuilder(string);
        while (stringBuilder.length() < size) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
