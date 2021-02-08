package com.senither.hypixel.commands.general;

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.*;

public class AmIBanned extends Command {

    public AmIBanned(SkyblockAssistant app) {
        super(app);

        setThrottleContainer(1, 10);
    }

    @Override
    public String getName() {
        return "Am I Banned?";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Checks if you're on a ban list registered with the bot, this will use the ban list",
            "from the `h!ban-log` to check if you're banned from any server using the bot."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Checks if you're on a ban-list registered with the bot.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("amibanned");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        try {
            UUID uuid = app.getHypixel().getUUIDFromUser(event.getAuthor());
            if (uuid == null) {
                MessageFactory.makeError(
                    event.getMessage(),
                    "Failed to find a valid UUID matching your Discord account, please try re-verify with the bot."
                ).queue();
                return;
            }

            Collection result = app.getDatabaseManager().query(
                "SELECT `ban_log`.*, `guilds`.`name` FROM `ban_log`" +
                    "LEFT JOIN `guilds` ON `guilds`.`discord_id` = `ban_log`.`discord_id`" +
                    "WHERE `uuid` = ?" +
                    "ORDER BY `created_at` DESC",
                uuid
            );

            if (result.isEmpty()) {
                MessageFactory.makeSuccess(event.getMessage(), "You have no entries in the ban-log.")
                    .setFooter("Requested by " + event.getAuthor().getAsTag(), event.getAuthor().getEffectiveAvatarUrl())
                    .queue();
                return;
            }

            List<MessageEmbed.Field> fields = new ArrayList<>();
            for (DataRow row : result) {
                if (row.getString("name") == null) {
                    continue;
                }

                fields.add(new MessageEmbed.Field(String.format(
                    "#%d - Added by %s from %s",
                    row.getLong("id"),
                    app.getHypixel().getUsernameFromUuid(UUID.fromString(row.getString("added_by"))),
                    row.getString("name")
                ), (row.getString("reason") == null
                    ? "_No reason was given_"
                    : row.getString("reason")
                ), false
                ));
            }

            SimplePaginator<MessageEmbed.Field> paginator = new SimplePaginator<>(fields, 5);
            if (args.length > 0) {
                paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
            }

            PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(), "")
                .setTitle("Ban log for " + app.getHypixel().getUsernameFromUuid(uuid));

            paginator.forEach((index, key, val) -> message.addField(val));

            message.addField(" ", paginator.generateFooter(String.format("%s%s",
                Constants.COMMAND_PREFIX, getTriggers().get(0)
            )), false).queue();
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "Something went wrong while trying to load the ban-log entries, error: " + e.getMessage()
            ).queue();
        }
    }
}
