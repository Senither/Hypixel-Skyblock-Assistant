package com.senither.hypixel.commands.administration;

import com.google.gson.reflect.TypeToken;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.*;

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
            listMessages(event, guildEntry, args);
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

        MessageContainer message;

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

            message = new MessageContainer(query.first());
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
                        setMessageVariableValue(event, message, Arrays.copyOfRange(args, 3, args.length));
                        break;

                    case "del":
                    case "rem":
                    case "delete":
                    case "remove":
                        removeMessageVariable(event, message, Arrays.copyOfRange(args, 3, args.length));
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

    private void listMessages(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, String[] args) {
        try {
            Collection result = app.getDatabaseManager().query("SELECT * FROM `messages` WHERE `discord_id` = ? ORDER BY `created_at` DESC", event.getGuild().getIdLong());
            if (result.isEmpty()) {
                MessageFactory.makeWarning(event.getMessage(),
                    "Found no messages for this server, you can create a new bot managed message using `:command`"
                ).set("command", Constants.COMMAND_PREFIX + "message create <channel> <message>").queue();
                return;
            }

            List<String> messages = new ArrayList<>();
            for (DataRow row : result) {
                MessageContainer container = new MessageContainer(row);

                TextChannel textChannelById = event.getGuild().getTextChannelById(container.getChannelId());
                if (textChannelById == null) {
                    continue;
                }

                String message = container.getFormattedMessage();
                messages.add(String.format("**[%s in %s](%s)**\n%s",
                    container.getMessageId(),
                    textChannelById.getAsMention(),
                    container.getMessageUrl(),
                    String.format("```%s...```", message.substring(0, Math.min(message.length(), 38)).trim())
                ));
            }

            int page = 1;
            if (args.length > 0) {
                page = NumberUtil.parseInt(args[0], 1);
            }

            SimplePaginator<String> paginator = new SimplePaginator<>(messages, 5, page);
            messages.clear();
            paginator.forEach((index, key, val) -> messages.add(val));

            MessageFactory.makeInfo(event.getMessage(), "")
                .setTitle("Tracked Messages in " + guildEntry.getName())
                .setDescription(String.format("%s\n%s",
                    String.join("\n", messages),
                    paginator.generateFooter(Constants.COMMAND_PREFIX + "message"))
                )
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editMessage(MessageReceivedEvent event, MessageContainer message, String[] args) {
        String[] rawParts = event.getMessage().getContentRaw().split(" ");
        String rawContent = String.join(" ", Arrays.copyOfRange(rawParts, 3, rawParts.length));

        message.setMessage(String.join(" ", rawContent));

        if (message.updateMessage(event.getGuild())) {
            MessageFactory.makeSuccess(event.getMessage(), "The message have been updated successfully!").queue();
        } else {
            MessageFactory.makeWarning(event.getMessage(), "Failed to update the message, does the message still exist?").queue();
        }
    }

    private void setMessageVariableValue(MessageReceivedEvent event, MessageContainer message, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the name of the variable you wish to setup on the message!"
            ).queue();
            return;
        }

        if (args.length == 1) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the value of the variable you wish to setup on the message!"
            ).queue();
            return;
        }

        String name = args[0];
        String value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        message.getVariables().put(name, value);

        if (message.updateMessage(event.getGuild())) {
            MessageFactory.makeSuccess(event.getMessage(), "The message have been updated successfully!").queue();
        } else {
            MessageFactory.makeWarning(event.getMessage(), "Failed to update the message, does the message still exist?").queue();
        }
    }

    private void removeMessageVariable(MessageReceivedEvent event, MessageContainer message, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the name of the variable you wish to remove from the message!"
            ).queue();
            return;
        }

        String name = args[0];

        if (!message.getVariables().containsKey(name)) {
            MessageFactory.makeError(event.getMessage(),
                "Found no variables in the current message called `:name`, please make sure the variable you're trying to remove exists."
            ).set("name", name).queue();
            return;
        }

        message.getVariables().remove(name);

        if (message.updateMessage(event.getGuild())) {
            MessageFactory.makeSuccess(event.getMessage(), "The variable have been successfully removed from the message!").queue();
        } else {
            MessageFactory.makeWarning(event.getMessage(), "Failed to update the message, does the message still exist?").queue();
        }
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

    class MessageContainer {

        private final long discordId;
        private final long channelId;
        private final long messageId;
        private String message;

        private final HashMap<String, String> variables = new HashMap<>();

        public MessageContainer(DataRow row) {
            this.discordId = row.getLong("discord_id");
            this.channelId = row.getLong("channel_id");
            this.messageId = row.getLong("message_id");
            this.message = row.getString("content");

            if (row.getString("variables") != null) {
                HashMap<String, String> variables = app.getHypixel().getGson().fromJson(
                    row.getString("variables"),
                    new TypeToken<HashMap<String, String>>() {
                    }.getType()
                );

                if (variables != null) {
                    for (Map.Entry<String, String> varEntry : variables.entrySet()) {
                        this.variables.put(varEntry.getKey(), varEntry.getValue());
                    }
                }
            }
        }

        public long getDiscordId() {
            return discordId;
        }

        public long getChannelId() {
            return channelId;
        }

        public long getMessageId() {
            return messageId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public HashMap<String, String> getVariables() {
            return variables;
        }

        public boolean updateMessage(Guild guild) {
            TextChannel textChannelById = guild.getTextChannelById(channelId);
            if (textChannelById == null) {
                return false;
            }

            try {
                String encodedMessage = "base64:" + new String(Base64.getEncoder().encode(message.getBytes()));
                String encodedVariables = variables.isEmpty() ? null
                    : "base64:" + new String(Base64.getEncoder().encode(app.getHypixel().getGson().toJson(variables).getBytes()));

                app.getDatabaseManager().queryUpdate("UPDATE `messages` SET `content` = ?, `variables` = ? WHERE `discord_id` = ? AND `message_id` = ?",
                    encodedMessage, encodedVariables, discordId, messageId
                );

                textChannelById.editMessageById(messageId, getFormattedMessage()).queue();
            } catch (SQLException e) {
                e.printStackTrace();

                return false;
            }

            return true;
        }

        public String getFormattedMessage() {
            if (variables.isEmpty()) {
                return message;
            }

            PlaceholderMessage message = new PlaceholderMessage(null, this.message);

            for (Map.Entry<String, String> varEntry : variables.entrySet()) {
                message.set(varEntry.getKey(), varEntry.getValue());
            }

            return message.toString();
        }

        public String getMessageUrl() {
            return String.format("https://discord.com/channels/%d/%d/%d",
                discordId, channelId, messageId
            );
        }
    }
}
