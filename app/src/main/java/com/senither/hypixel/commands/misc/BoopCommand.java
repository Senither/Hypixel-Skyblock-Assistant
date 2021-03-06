package com.senither.hypixel.commands.misc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BoopCommand extends Command {

    public static final Cache<Long, Boolean> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .recordStats()
        .build();

    public BoopCommand(SkyblockAssistant app) {
        super(app, false);

        setThrottleContainer(2, 10);
    }

    @Override
    public String getName() {
        return "Boop Command";
    }

    @Override
    public List<String> getDescription() {
        return Collections.singletonList("Boops the mentioned user, sending them a DM or pinging them directly on Discord.");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user>` - Boops the the mentioned user.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command Senither`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("boop");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must mention a user you want to boop."
            ).queue();
            return;
        }

        User mentionedUser = getMentionedUser(event, args);
        if (mentionedUser == null) {
            MessageFactory.makeError(event.getMessage(),
                "Invalid user provided, found no users with the name **:name**"
            ).set("name", String.join(" ", args)).queue();
            return;
        }

        if (!isOptIn(mentionedUser.getIdLong())) {
            MessageFactory.makeWarning(event.getMessage(),
                ":user has opt-out of getting boop notifications!\nYou can't boop this person."
            ).set("user", mentionedUser.getAsMention()).queue();
            return;
        }

        mentionedUser.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(MessageFactory.makeEmbeddedMessage(null)
                .setDescription("**Boop!**\nYou have just been booped by :user from **:guild** in :channel")
                .setFooter("You can opt-out of getting boop notifications by using \"h!boop-opt out\"")
                .set("user", event.getAuthor().getAsMention())
                .set("guild", event.getGuild().getName())
                .set("channel", event.getTextChannel().getAsMention())
                .buildEmbed()
            ).queue();

            MessageFactory.makeSuccess(event.getMessage(), ":user have been booped!")
                .set("user", mentionedUser.getAsMention())
                .queue();
        }, error -> {
            event.getChannel().sendMessage(
                (new MessageBuilder())
                    .setContent(mentionedUser.getAsTag())
                    .setEmbed(MessageFactory.makeEmbeddedMessage(event.getChannel())
                        .setDescription(":target have been booped by :user!")
                        .set("target", mentionedUser.getAsMention())
                        .set("user", event.getAuthor().getAsMention())
                        .buildEmbed()
                    ).build()
            ).queue();
        });
    }

    private User getMentionedUser(MessageReceivedEvent event, String[] args) {
        if (!event.getMessage().getMentionedUsers().isEmpty()) {
            return event.getMessage().getMentionedUsers().get(0);
        }

        List<Member> members = event.getGuild().getMembersByEffectiveName(
            String.join(" ", args), true
        );

        if (!members.isEmpty()) {
            return members.get(0).getUser();
        }

        return null;
    }

    private boolean isOptIn(long id) {
        Boolean ifPresent = cache.getIfPresent(id);
        if (ifPresent != null) {
            return ifPresent;
        }

        try {
            boolean isOptIn = app.getDatabaseManager().query(
                "SELECT * FROM `boop_opt` WHERE `discord_id` = ?", id
            ).isEmpty();

            cache.put(id, isOptIn);

            return isOptIn;
        } catch (SQLException e) {
            e.printStackTrace();

            return true;
        }
    }
}
