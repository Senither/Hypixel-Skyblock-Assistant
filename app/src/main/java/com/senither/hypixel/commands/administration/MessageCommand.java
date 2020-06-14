package com.senither.hypixel.commands.administration;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class MessageCommand extends Command {

    public MessageCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Message Command";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "TODO"
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command [page]` - List messages that is managed by the bot",
            "`:command create <channel> <message>` - Edits the message with the given ID",
            "`:command edit <id> <message>` - Edits the message with the given ID",
            "`:command var <id> set <name> <value>` - Sets the value of a variable for the given message ID",
            "`:command var <id> remove <name>` - Deletes the given variable for the given message ID"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "TODO"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("message", "messages", "announcement");
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

        if (!isGuildMasterOrOfficerOfServerGuild(event, guildEntry)) {
            MessageFactory.makeError(event.getMessage(),
                "You must be the guild master or an officer of the **:name** guild to use this command!"
            ).set("name", guildEntry.getName()).setTitle("Missing argument").queue();
            return;
        }

        if (args.length == 0 || NumberUtil.isNumeric(args[0])) {
            listMessages(event, args);
            return;
        } else if ("create".equalsIgnoreCase(args[0]) || "new".equalsIgnoreCase(args[0])) {
            createNewMessage(event, Arrays.copyOfRange(args, 1, args.length));
            return;
        }

        if (args.length < 2) {
            MessageFactory.makeError(event.getMessage(),
                "You must include both the action you wish to preform, and the ID of a valid bot message you wish to manage!"
            ).queue();
            return;
        }

        DataRow message = null;

        try {
            Collection query = app.getDatabaseManager().query("SELECT * FROM `messages` WHERE `discord_id` = ? AND `message_id` = ?",
                event.getGuild().getIdLong(), args[1]
            );

            if (query.isEmpty()) {
                MessageFactory.makeError(event.getMessage(),
                    "Invalid message ID given! You must provide the ID of a message managed by the bot!"
                ).queue();
                return;
            }

            message = query.first();
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to load the message data from the database, error: " + e.getMessage()
            ).queue();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "edit":
            case "modify":
                editMessage(event, message, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "var":
            case "variable":
                if (args.length == 2) {
                    MessageFactory.makeError(event.getMessage(),
                        "You must provide the variable action you wish to preform for the given message ID!"
                    ).queue();
                    break;
                }

                switch (args[2].toLowerCase()) {
                    case "set":
                    case "edit":
                    case "modify":
                        setMessageVariableValue(event, message, Arrays.copyOfRange(args, 2, args.length));
                        break;

                    case "del":
                    case "rem":
                    case "delete":
                    case "remove":
                        removeMessageVariable(event, message, Arrays.copyOfRange(args, 2, args.length));
                        break;

                    default:
                        MessageFactory.makeWarning(event.getMessage(),
                            "Invalid message variable action given, the action must either be `set` or `remove`"
                        ).queue();
                        break;
                }
                break;

            default:
                MessageFactory.makeWarning(event.getMessage(),
                    "Invalid message action given, the action must either be `set` or `var`"
                ).queue();
        }
    }

    private void createNewMessage(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the channel you wish to send the message in!"
            ).queue();
            return;
        }

        TextChannel channel = getChannelFromMessage(event.getMessage(), args);
        if (channel == null) {
            MessageFactory.makeError(event.getMessage(),
                "You must provide a valid name, or mention an existing text channel you wish to send the message in."
            ).queue();
            return;
        }

        if (!channel.canTalk()) {
            MessageFactory.makeError(event.getMessage(),
                "The bot does not have permissions to send messages in the :name channel!"
            ).set("name", channel.getAsMention()).queue();
            return;
        }

        String[] rawParts = event.getMessage().getContentRaw().split(" ");
        String rawContent = String.join(" ", Arrays.copyOfRange(rawParts, 3, rawParts.length));

        channel.sendMessage(rawContent).queue(message -> {
            String encodedContent = "base64:" + new String(Base64.getEncoder().encode(rawContent.getBytes()));

            try {
                app.getDatabaseManager().queryInsert("INSERT `messages` SET `discord_id` = ?, `channel_id` = ?, `message_id` = ?, `content` = ?",
                    event.getGuild().getIdLong(),
                    channel.getIdLong(),
                    message.getIdLong(),
                    encodedContent
                );

                MessageFactory.makeSuccess(event.getMessage(),
                    "The message have been send and registered successfully! The message is not being tracked by the bot."
                ).queue();
            } catch (SQLException e) {
                MessageFactory.makeError(event.getMessage(),
                    "Failed to register the message, error: " + e.getMessage()
                ).queue();
            }
        }, error -> {
            MessageFactory.makeError(event.getMessage(),
                "Failed to send the message to the :name channel, error: " + error.getMessage()
            ).set("name", channel.getAsMention()).queue();
        });
    }

    private void listMessages(MessageReceivedEvent event, String[] args) {
        MessageFactory.makeInfo(event.getMessage(), "listMessages(MessageReceivedEvent event,String[] args)").queue();
    }

    private void editMessage(MessageReceivedEvent event, DataRow message, String[] args) {
        MessageFactory.makeInfo(event.getMessage(), "editMessage(MessageReceivedEvent event, DataRow message, String[] args)").queue();
    }

    private void setMessageVariableValue(MessageReceivedEvent event, DataRow message, String[] args) {
        MessageFactory.makeInfo(event.getMessage(), "setMessageVariableValue(MessageReceivedEvent event, DataRow message, String[] args)").queue();
    }

    private void removeMessageVariable(MessageReceivedEvent event, DataRow message, String[] args) {
        MessageFactory.makeInfo(event.getMessage(), "removeMessageVariable(MessageReceivedEvent event, DataRow message, String[] args)").queue();
    }

    private TextChannel getChannelFromMessage(Message message, String[] args) {
        if (!message.getMentionedChannels().isEmpty()) {
            return message.getMentionedChannels().get(0);
        }

        if (args.length == 0) {
            return null;
        }

        String part = args[0].trim();

        if (NumberUtil.isNumeric(part)) {
            return message.getGuild().getTextChannelById(part);
        }

        List<TextChannel> channelsByName = message.getGuild().getTextChannelsByName(part, true);
        return channelsByName.isEmpty() ? null : channelsByName.get(0);
    }
}
