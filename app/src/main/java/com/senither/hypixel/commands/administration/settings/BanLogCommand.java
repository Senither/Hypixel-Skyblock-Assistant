package com.senither.hypixel.commands.administration.settings;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.SettingsSubCommand;
import com.senither.hypixel.database.controller.GuildController;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BanLogCommand extends SettingsSubCommand {

    private static final Logger log = LoggerFactory.getLogger(BanLogCommand.class);

    public BanLogCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Ban Log";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to setup a ban-log role, when the role is setup anyone",
            "with the role will be able to use the ban-log, the guild master and guild",
            "officers won't need the Discord role to use the ban-log."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <role name>` - Sets the ban-log role that should be used.",
            "`:command disable` - Disables the ban-log role feature."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command Staff`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("ban-log", "banlog", "log", "bl");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the name of the role you wish to use as "
                    + "the ban-log role for the server, or `disable` to disable the "
                    + "ban-log feature."
            ).setTitle("Missing argument").queue();
            return;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            handleDisablingFeature(event);
            return;
        }

        List<Role> roles = event.getGuild().getRolesByName(String.join(" ", args), true);
        if (roles.isEmpty()) {
            MessageFactory.makeError(event.getMessage(), "Failed to find any roles called `:name`, are you sure the role exists?")
                .set("name", String.join(" ", args))
                .queue();
            return;
        }

        Role selectedRole = roles.get(0);

        try {
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `ban_log_role` = ? WHERE `discord_id` = ?",
                selectedRole.getIdLong(), event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(),
                "The :role role will now be used as the ban-log role!"
            ).set("role", selectedRole.getAsMention()).queue();
        } catch (SQLException e) {
            log.error("Failed to store the selected role ({}) in the database for {}, error: {}",
                selectedRole.getIdLong(), event.getGuild().getIdLong(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Failed to store the ban-log role in the database, error: :error")
                .set("error", e.getMessage())
                .queue();
        }
    }

    private void handleDisablingFeature(MessageReceivedEvent event) {
        try {
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `ban_log_role` = NULL WHERE `discord_id` = ?", event.getGuild().getIdLong());

            GuildController.forgetCacheFor(event.getGuild().getIdLong());

            MessageFactory.makeSuccess(event.getMessage(), "The ban-log feature have now been disabled!").queue();
        } catch (SQLException e) {
            log.error("Failed to set the default role for {} to null during reset, error: {}",
                event.getGuild().getId(), e.getMessage(), e
            );

            MessageFactory.makeError(event.getMessage(), "Something went wrong while trying to reset the servers ban-log role!").queue();
        }
    }
}
