package com.senither.hypixel.commands.administration;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class BanLogCommand extends Command {

    public BanLogCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Ban Log Command";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to view, add, or remove people from the ban log,",
            "the ban log allows you to view people who are banned, and for what",
            "reason across the entire bot."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command add <user> <reason>` - Adds the user to the ban log.",
            "`:command remove <user> <id>` - Removes the entry with the given ID from the user in the ban log.",
            "`:command view <user>` - Views a users ban log status."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command add Senither Being a dumb dumb`",
            "`:command remove Senither 9`",
            "`:command view Senither`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("ban-log", "banlog", "log", "bl");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "Missing the `name` of the action you wish to preform!"
            ).setTitle("Missing argument").queue();
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "Missing the `username` of the user you wish to use the ban log action on."
            ).setTitle("Missing argument").queue();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add":
            case "adds":
                addUser(event, args[1], Arrays.copyOfRange(args, 2, args.length));
                break;

            case "show":
            case "view":
                showUser(event, args[1], Arrays.copyOfRange(args, 2, args.length));
                break;

            case "remove":
            case "delete":
                removeUser(event, args[1], Arrays.copyOfRange(args, 2, args.length));
                break;

            default:
                MessageFactory.makeWarning(event.getMessage(),
                    "Invalid ban log action given! `:type` is not a valid ban log action."
                ).set("type", args[0]).queue();
        }
    }

    private void addUser(MessageReceivedEvent event, String username, String[] args) {
        UUID uuid = getUuidFromUsername(event, username);
        if (uuid == null) {
            MessageFactory.makeWarning(event.getMessage(),
                "Found no Minecraft user account with the name `:name`, please make sure you have entered the username correctly!"
            ).set("name", username).queue();
            return;
        }

        UUID userUUID = getCurrentUserUUID(event);
        if (userUUID == null) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to retrieve your user UUID, please try again in a minute."
            ).queue();
            return;
        }

        String reason = null;
        if (args.length > 0) {
            reason = "base64:" + new String(
                Base64.getEncoder().encode(
                    String.join(" ", args).getBytes()
                )
            );
        }

        try {
            app.getDatabaseManager().queryInsert(
                "INSERT INTO `ban_log` SET `discord_id` = ?, `added_by` = ?, `uuid` = ?, `reason` = ?;",
                event.getGuild().getIdLong(), userUUID, uuid, reason
            );

            MessageFactory.makeSuccess(event.getMessage(),
                "**:name** have been added to the ban-log, :reason"
            ).set("name", username).set("reason", reason == null
                ? "with *no reason* given"
                : "for \"" + String.join(" ", args) + "\""
            ).queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showUser(MessageReceivedEvent event, String username, String[] args) {
        UUID uuid = getUuidFromUsername(event, username);
        if (uuid == null) {
            MessageFactory.makeWarning(event.getMessage(),
                "Found no Minecraft user account with the name `:name`, please make sure you have entered the username correctly!"
            ).set("name", username).queue();
            return;
        }
    }

    private void removeUser(MessageReceivedEvent event, String username, String[] args) {
        UUID uuid = getUuidFromUsername(event, username);
        if (uuid == null) {
            MessageFactory.makeWarning(event.getMessage(),
                "Found no Minecraft user account with the name `:name`, please make sure you have entered the username correctly!"
            ).set("name", username).queue();
            return;
        }
    }

    private UUID getCurrentUserUUID(MessageReceivedEvent event) {
        try {
            return app.getHypixel().getUUIDFromUser(event.getAuthor());
        } catch (SQLException ignored) {
            return null;
        }
    }

    private UUID getUuidFromUsername(MessageReceivedEvent event, String username) {
        try {
            return app.getHypixel().getUUIDFromName(username);
        } catch (SQLException e) {
            return null;
        }
    }
}
