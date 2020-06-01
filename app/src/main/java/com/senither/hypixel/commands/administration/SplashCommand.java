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
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.splash.SplashContainer;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(SplashCommand.class);

    private final Pattern timeRegEx = Pattern.compile("([0-9]+[w|d|h|m|s])");

    public SplashCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Splash Command";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to track splashes in a guild, allowing the officers",
            "to easily see who splashes the most, when the last splash was, queue up",
            "splashes for the future, and see statistics for everyone who",
            "splashes pots in the guild.",
            "\nPeople with the splash role are able to edit splash messages, cancel splashes,",
            "and completely remove splashes from the tracking system from people."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <now|time> <message>` - Queues up a new splash",
            "`:command list [page]` - Shows the splash leaderboard",
            "`:command cancel [id]` - Cancels the splash with the given ID, or the last splash created by the player",
            "`:command edit [id]` - Edits the message of the splash with the given ID, or the last splash created by the player",
            "`:command remove <id>` - Removes the splash with the given ID from the splash tracker",
            "`:command show <username>` - Lookup splash stats for the given player"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command now Splashing at pond` - Creates a splash now",
            "`:command 1h9m pond g party` - Queues a splash that ends in 1 hour and 9 minutes",
            "`:command list 3` - Shows the 3rd page of the splash leaderboard",
            "`:command cancel 42` - Cancels the queued splash with an ID of 42",
            "`:command edit 75 Some new message` - Sets a new message for the splash with an ID of 75",
            "`:command remove 39` - Removes the splash with an ID of 39 from the splash tracker",
            "`:command show Senither` - Show splash stats for Senither"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("splash", "splashes");
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

        try {
            if (!isMemberOfGuild(guildEntry, app.getHypixel().getUUIDFromUser(event.getAuthor()))) {
                MessageFactory.makeWarning(event.getMessage(),
                    "You're not a member of the **:name** guild!\nYou can't use this command if you're not a member of the guild."
                ).set("name", guildEntry.getName()).queue();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!guildEntry.isSplashTrackerEnabled()) {
            MessageFactory.makeError(event.getMessage(),
                "The splash tracker feature have not yet been enabled for the server, you "
                    + "must setup the feature before being able to use this command, you can enable the "
                    + "feature by running:"
                    + "\n```h!settings splash <channel> <role>```"
            ).setTitle("Splash tracker is not setup").queue();
            return;
        }

        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(), "Some error")
                .queue();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "ls":
            case "list":
            case "leaderboard":
                showLeaderboard(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "stop":
            case "cancel":
                cancelSplash(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "remove":
            case "revoke":
                removeSplash(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "edit":
                editSplash(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "show":
            case "look":
            case "view":
            case "lookup":
            case "overview":
                lookupSplashForPlayer(guildEntry, event, Arrays.copyOfRange(args, 1, args.length));
                break;

            default:
                createSplash(guildEntry, event, args);
        }
    }

    private void showLeaderboard(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        final String leaderboardQuery = "SELECT `id`, `uuid`, COUNT(`id`) AS 'total' FROM `splashes`\n" +
            "\tWHERE `discord_id` = ? AND `splash_at` > ?\n" +
            "\tGROUP BY `uuid`\n" +
            "\tORDER BY `total` DESC;";

        final int page = args.length == 0
            ? 0 : Math.max(NumberUtil.parseInt(args[0], 1), 1);

        try {
            Collection stats = app.getDatabaseManager().query(leaderboardQuery, event.getGuild().getIdLong(), Carbon.now().subDays(28));

            HashSet<String> uuids = new HashSet<>();
            for (DataRow stat : stats) {
                uuids.add(stat.getString("uuid"));
            }

            StringBuilder stringifiedParams = new StringBuilder();
            for (String ignored : uuids) {
                stringifiedParams.append("?, ");
            }

            Collection usernameQueryResult = app.getDatabaseManager().query(String.format(
                "SELECT `uuid`, `username` FROM `uuids` WHERE `uuid` IN (%s);",
                stringifiedParams.toString().substring(0, stringifiedParams.length() - 2)
            ), uuids.toArray());

            HashMap<String, String> usernameMap = new HashMap<>();
            for (DataRow row : usernameQueryResult) {
                usernameMap.put(row.getString("uuid"), row.getString("username"));
            }

            int position = 1;
            List<String> leaderboardEntries = new ArrayList<>();
            for (DataRow stat : stats) {
                int splashes = stat.getInt("total");

                leaderboardEntries.add(String.format("%s: %s\n%s> %s (%s/daily)",
                    padPosition("#" + NumberUtil.formatNicely(position), position - 1),
                    usernameMap.getOrDefault(stat.getString("uuid"), "Unknown"),
                    padPosition("", position - 1),
                    NumberUtil.formatNicely(splashes),
                    NumberUtil.formatNicelyWithDecimals(splashes / 28D)
                ));

                position++;
            }

            SimplePaginator<String> paginator = new SimplePaginator<>(leaderboardEntries, 10, page);
            leaderboardEntries.clear();
            paginator.forEach((index, key, val) -> leaderboardEntries.add(val));

            MessageFactory.makeInfo(event.getMessage(), String.format(
                "The splash leaderboard are a list players who has splashed in the last month. %s\n%s",
                String.format(
                    "```ada\n%s```",
                    String.join("\n", leaderboardEntries)
                ), paginator.generateFooter(Constants.COMMAND_PREFIX + getTriggers().get(0) + " list"))
            )
                .setTitle(guildEntry.getName() + " Splash Leaderboard")
                .setTimestamp(Carbon.now().getTime().toInstant())
                .queue();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void cancelSplash(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        int splashId = args.length == 0 ? 0 : Math.max(NumberUtil.parseInt(args[0], 0), 0);

        //noinspection ConstantConditions
        SplashContainer splashContainer = null;
        try {
            splashContainer = splashId == 0
                ? app.getSplashManager().getEarliestSplashFromUser(
                app.getHypixel().getUUIDFromUser(event.getAuthor())
            ) : app.getSplashManager().getPendingSplashById(splashId);
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to load your UUID from the database, error: " + e.getMessage()
            ).queue();
            return;
        }

        if (splashContainer == null) {
            MessageFactory.makeError(event.getMessage(),
                splashId == 0
                    ? "You don't have any splashes queued to cancel right now!"
                    : "Found no splash that is queued with an ID of **:id** that was created by you."
            ).set("id", NumberUtil.formatNicely(splashId)).queue();
            return;
        }

        UUID uuid = getUUIDFromUser(event.getAuthor());
        if (!splashContainer.getUserUuid().equals(uuid) && !isOfficerInGuildOrHasSplashRole(event, guildEntry, uuid)) {
            MessageFactory.makeWarning(event.getMessage(),
                "You cannot cancel a splash created by other users without having the <@&:id> role, or being an officer of the guild."
            ).set("id", guildEntry.getSplashRole()).queue();
            return;
        }

        try {
            app.getSplashManager().cancelSplash(splashContainer).get();

            MessageFactory.makeInfo(event.getMessage(),
                "The splash with an ID of **:id** have been canceled successfully!"
            )
                .setTitle("Splash has been canceled!")
                .set("id", splashContainer.getId())
                .queue();
        } catch (InterruptedException | ExecutionException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to cancel splash with an ID of **:id**, error: :message"
            ).set("id", splashContainer.getId()).set("message", e.getMessage()).queue();

            event.getMessage().delete().queue();

            log.error("Something went wrong while trying to cancel splash with an ID of {}, error: {}",
                splashContainer.getId(), e.getMessage(), e
            );
        }
    }

    private void removeSplash(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        if (!isOfficerInGuildOrHasSplashRole(event, guildEntry, getUUIDFromUser(event.getAuthor()))) {
            MessageFactory.makeWarning(event.getMessage(),
                "You must have the <@&:id> role, or be an officer of the **:name** guild to remove splashes from the splash tracker."
            ).set("id", guildEntry.getSplashRole()).queue();
            return;
        }

        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the `id` of the splash to wish to remove from the splash history, you can see the splash ID at the bottom of each splash message."
            ).queue();
            return;
        }
        int splashId = Math.max(NumberUtil.parseInt(args[0], 0), 0);

        //noinspection ConstantConditions
        SplashContainer splashContainer = null;
        try {
            splashContainer = app.getSplashManager().getSplashById(splashId);
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to load your UUID from the database, error: " + e.getMessage()
            ).queue();
            return;
        }

        if (splashContainer == null || splashContainer.getDiscordId() != event.getGuild().getIdLong()) {
            MessageFactory.makeError(event.getMessage(),
                "Found no splash from this guild with an ID of **:id**."
            ).set("id", NumberUtil.formatNicely(splashId)).queue();
            return;
        }

        try {
            boolean result = app.getSplashManager().removeSplashById(splashContainer).get(5, TimeUnit.SECONDS);

            MessageFactory.makeSuccess(event.getMessage(),
                "The splash has successfully been deleted from the splash tracker!" + (result
                    ? "\nThe splash message has also been deleted !" : ""
                )).queue();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void editSplash(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the message that should be set for your last splash, or the splash with the given ID."
            ).queue();
            return;
        }

        int splashId = Math.max(NumberUtil.parseInt(args[0], 0), 0);

        //noinspection ConstantConditions
        SplashContainer splashContainer = null;
        try {
            splashContainer = splashId == 0
                ? app.getSplashManager().getEarliestSplashFromUser(
                app.getHypixel().getUUIDFromUser(event.getAuthor())
            ) : app.getSplashManager().getPendingSplashById(splashId);
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to load your UUID from the database, error: " + e.getMessage()
            ).queue();
            return;
        }

        if (splashContainer == null) {
            MessageFactory.makeError(event.getMessage(),
                splashId == 0
                    ? "You don't have any splashes queued to change the message of right now!"
                    : "Found no splash that is queued with an ID of **:id** that was created by you."
            ).set("id", NumberUtil.formatNicely(splashId)).queue();
            return;
        }


        UUID uuid = getUUIDFromUser(event.getAuthor());
        boolean officerInGuildOrHasSplashRole = isOfficerInGuildOrHasSplashRole(event, guildEntry, uuid);
        if (!splashContainer.getUserUuid().equals(uuid) && !officerInGuildOrHasSplashRole) {
            MessageFactory.makeWarning(event.getMessage(),
                "You cannot edit a splash message created by other users without having the <@&:id> role, or being an officer of the guild."
            ).set("id", guildEntry.getSplashRole()).queue();
            return;
        }

        Long discordUserId = app.getHypixel().getDiscordIdFromUUID(splashContainer.getUserUuid());
        if (event.getAuthor().getIdLong() != discordUserId && !officerInGuildOrHasSplashRole) {
            MessageFactory.makeError(event.getMessage(),
                "You're not the author of the queued splash with an ID of **:id**!\nYou can't the splash message of splashes you didn't create."
            ).set("id", splashContainer.getId()).queue();
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(
            args, splashId == 0 ? 0 : 1, args.length
        ));

        if (message.trim().isEmpty()) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the message you want to set for the queued splash with an ID of **:id**!"
            ).set("id", NumberUtil.formatNicely(splashContainer.getId())).queue();
            return;
        }

        try {
            app.getDatabaseManager().queryUpdate("UPDATE `splashes` SET `note` = ? WHERE `discord_id` = ? AND `id` = ?",
                "base64:" + new String(Base64.getEncoder().encode(message.getBytes())),
                splashContainer.getDiscordId(), splashContainer.getId()
            );

            splashContainer.setNote(message);
            app.getSplashManager().updateSplashFor(splashContainer);

            MessageFactory.makeSuccess(event.getMessage(),
                "The splash message with an ID of **:id** have been successfully change to:\n\n> :message"
            ).set("id", NumberUtil.formatNicely(splashContainer.getId())).set("message", message).queue();
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to save the new message note, error: :message"
            ).set("message", e.getMessage()).queue();

            log.error("An SQL Exception were thrown while trying to update the splash container with an ID of {}, error: {}",
                splashContainer.getId(), e.getMessage(), e
            );
        }
    }

    private void lookupSplashForPlayer(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        UUID uuid = null;
        try {
            uuid = app.getHypixel().getUUIDFromName(args[0]);
        } catch (SQLException ignored) {
            throw new FriendlyException("Failed to find a UUID matching the given username!");
        }

        try {
            Collection query = app.getDatabaseManager().query(
                "SELECT\n" +
                    "    COUNT(`id`) AS 'total',\n" +
                    "    (SELECT COUNT(`id`) FROM `splashes` WHERE `splash_at` > ? AND `discord_id` = ? AND `uuid` = ?) AS 'hour',\n" +
                    "    (SELECT COUNT(`id`) FROM `splashes` WHERE `splash_at` > ? AND `discord_id` = ? AND `uuid` = ?) AS 'week',\n" +
                    "    (SELECT COUNT(`id`) FROM `splashes` WHERE `splash_at` > ? AND `discord_id` = ? AND `uuid` = ?) AS 'month'\n" +
                    "    FROM `splashes` WHERE `discord_id` = ? AND `uuid` = ?;",
                Carbon.now().subHours(24), event.getGuild().getIdLong(), uuid,
                Carbon.now().subDays(7), event.getGuild().getIdLong(), uuid,
                Carbon.now().subDays(28), event.getGuild().getIdLong(), uuid,
                event.getGuild().getIdLong(), uuid
            );

            if (query.isEmpty()) {
                MessageFactory.makeWarning(event.getMessage(), "Found no splash history records for :user!")
                    .set("user", app.getHypixel().getUsernameFromUuid(uuid))
                    .queue();
                return;
            }

            Collection latestSplashesResults = app.getDatabaseManager().query(
                "SELECT * FROM `splashes` WHERE `discord_id` = ? AND `uuid` = ? ORDER BY `splash_at` DESC LIMIT 5",
                event.getGuild().getIdLong(), uuid
            );

            int index = 1;
            List<String> splashHistory = new ArrayList<>();
            for (DataRow row : latestSplashesResults) {
                splashHistory.add(String.format(
                    "#%s : %s\n---- %s",
                    index++,
                    row.getTimestamp("splash_at").diffForHumans(),
                    row.getString("note")
                ));
            }

            DataRow metrics = query.first();

            PlaceholderMessage message = MessageFactory.makeSuccess(event.getMessage(), "")
                .setTitle("Splash History for " + app.getHypixel().getUsernameFromUuid(uuid))
                .addField("Last 24 Hours", formatSplashes(metrics.getLong("hour"), TimeUnit.HOURS, 24), true)
                .addField("Last Week", formatSplashes(metrics.getLong("week"), TimeUnit.DAYS, 7), true)
                .addField("Last Month", formatSplashes(metrics.getLong("month"), TimeUnit.DAYS, 28), true)
                .setFooter("Total splashes: " + NumberUtil.formatNicely(metrics.getLong("total")))
                .set("user", event.getAuthor().getAsMention());

            if (!splashHistory.isEmpty()) {
                message.addField(String.format(
                    "Latest Splash History (%s)",
                    splashHistory.size()
                ), String.format(
                    "```elm\n%s```",
                    String.join("\n", splashHistory)
                ), true);
            }

            message.queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String formatSplashes(long splashes, TimeUnit unit, double time) {
        final String name = unit.name().toLowerCase();

        return String.format("%s\n%s per %s",
            NumberUtil.formatNicely(splashes),
            NumberUtil.formatNicelyWithDecimals(
                splashes == 0 ? 0 : splashes / time
            ), name.substring(0, name.length() - 1)
        );
    }

    private void createSplash(GuildController.GuildEntry guildEntry, MessageReceivedEvent event, String[] args) {
        TextChannel splashChannel = app.getShardManager().getTextChannelById(guildEntry.getSplashChannel());
        if (splashChannel == null) {
            MessageFactory.makeError(event.getMessage(),
                "The splash channel does not appear to exist, have it been deleted?"
            ).queue();
            return;
        }

        Carbon time = parseTime(args[0]);
        if (time == null) {
            MessageFactory.makeError(event.getMessage(),
                "The given time is not valid, please provide a valid time for when the splash should happen."
            ).queue();
            return;
        }

        String note = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (note.isEmpty()) {
            MessageFactory.makeWarning(event.getMessage(),
                "You must include a note for the splash, something like the location, or what is being splashed."
            ).setTitle("Missing splash note").queue();
            return;
        }

        try {
            app.getSplashManager().createSplash(
                splashChannel,
                event.getAuthor(),
                time,
                String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            ).get();

            MessageFactory.makeInfo(event.getMessage(),
                "The splash have been registered successfully!"
            )
                .setTitle("Splash has been created!")
                .setFooter("Splasher: " + event.getAuthor().getAsTag())
                .queue();

            event.getMessage().delete().queue();
        } catch (InterruptedException | ExecutionException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to register the splash, error: " + e.getMessage()
            ).queue();

            event.getMessage().delete().queue();

            log.error("Something went wrong while trying to register splash, error: {}", e.getMessage(), e);
        }
    }

    private Carbon parseTime(String string) {
        if ("now".equalsIgnoreCase(string)) {
            return Carbon.now();
        }

        Matcher matcher = timeRegEx.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        Carbon time = Carbon.now();
        do {
            String group = matcher.group();

            String type = group.substring(group.length() - 1, group.length());
            int timeToAdd = NumberUtil.parseInt(group.substring(0, group.length() - 1), 0);

            switch (type.toLowerCase()) {
                case "w":
                    time.addWeeks(timeToAdd);
                    break;

                case "d":
                    time.addDays(timeToAdd);
                    break;

                case "h":
                    time.addHours(timeToAdd);
                    break;

                case "m":
                    time.addMinutes(timeToAdd);
                    break;

                case "s":
                    time.addSeconds(timeToAdd);
                    break;
            }
        } while (matcher.find());

        return time;
    }

    private String padPosition(String string, double position) {
        StringBuilder builder = new StringBuilder(string);
        while (builder.length() < 1 + String.valueOf(position).length()) {
            builder.append(" ");
        }
        return builder.toString();
    }

    private boolean isMemberOfGuild(GuildController.GuildEntry guildEntry, UUID uuid) {
        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);
        if (guildReply == null || guildReply.getGuild() == null) {
            throw new FriendlyException("The request to the API returned null for a guild with the given name, try again later.");
        }

        for (GuildReply.Guild.Member member : guildReply.getGuild().getMembers()) {
            if (member.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOfficerInGuildOrHasSplashRole(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, UUID uuid) {
        if (event.getMember() != null) {
            for (Role role : event.getMember().getRoles()) {
                if (role.getIdLong() == guildEntry.getSplashRole()) {
                    return true;
                }
            }
        }
        return isGuildMasterOrOfficerOfServerGuild(event, guildEntry);
    }
}
