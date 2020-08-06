package com.senither.hypixel.commands.misc;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoopOptCommand extends Command {

    public BoopOptCommand(SkyblockAssistant app) {
        super(app);

        this.setVisible(false);
    }

    @Override
    public String getName() {
        return "Boop opt in/out";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to opt out of getting boop notifications,",
            "this means that people will no longer be able to boop you through",
            "the bot, the same command can also be used to opt back in to",
            "getting boop notifications."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command out` - Opts out of getting boop messages",
            "`:command in` - Opts in of getting boop messages"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command out`",
            "`:command in`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("boop-opt");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must use either `in` to opt into boop notifications, or `out` to opt out of boop notifications."
            ).setTitle("Missing argument").queue();
            return;
        }

        BoopState state = BoopState.fromName(args[0]);
        if (state == null) {
            MessageFactory.makeError(event.getMessage(),
                "Invalid option give, you can only use `in` to opt-in, or `out` to opt-out."
            ).queue();
            return;
        }

        try {
            boolean isOptIn = app.getDatabaseManager().query("SELECT * FROM `boop_opt` WHERE `discord_id` = ?", event.getAuthor().getIdLong()).isEmpty();

            if (state.equals(BoopState.IN) && !isOptIn) {
                app.getDatabaseManager().queryUpdate("DELETE FROM `boop_opt` WHERE `discord_id` = ?", event.getAuthor().getIdLong());
            } else if (state.equals(BoopState.OUT) && isOptIn) {
                app.getDatabaseManager().queryInsert("INSERT INTO `boop_opt` SET `discord_id` = ?", event.getAuthor().getIdLong());
            }

            BoopCommand.cache.invalidate(event.getAuthor().getIdLong());

            MessageFactory.makeInfo(event.getMessage(), "You have now opt-:state of getting boop notifications.")
                .set("state", state.name().toLowerCase())
                .queue();
        } catch (SQLException e) {
            MessageFactory.makeError(event.getMessage(),
                "An error occurred while attempting to opt-:state, error: :message"
            ).set("state", state.name().toLowerCase()).set("message", e.getMessage()).queue();

            e.printStackTrace();
        }
    }

    enum BoopState {
        IN, OUT;

        static BoopState fromName(String name) {
            for (BoopState value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }
    }
}
