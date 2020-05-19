package com.senither.hypixel.splash;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.time.Carbon;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SplashManager {

    private static final Logger log = LoggerFactory.getLogger(SplashManager.class);

    private final SkyblockAssistant app;
    private final Set<SplashContainer> splashes;

    public SplashManager(SkyblockAssistant app) {
        this.app = app;
        this.splashes = new HashSet<>();
    }

    public Set<SplashContainer> getSplashes() {
        return splashes;
    }

    public void updateSplashFor(SplashContainer splash) {
        // TODO: This is hardcoded for now, but should be moved to the guild entry
        long channelId = 684837094111182963L;

        User userById = app.getShardManager().getUserById(splash.getUserId());
        if (userById == null) {
            return;
        }

        TextChannel channelById = app.getShardManager().getTextChannelById(channelId);
        if (channelById == null) {
            return;
        }

        channelById.editMessageById(splash.getMessageId(), buildSplashMessage(
            userById, splash.getTime(), splash.getNote()
        )).queue();
    }

    public CompletableFuture<Void> createSplash(TextChannel channel, User author, Carbon time, String note) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        final boolean isNow = time.diffInSeconds(Carbon.now()) <= 5;

        channel.sendMessage(buildSplashMessage(author, time, note)).queue(message -> {
            try {
                app.getDatabaseManager().queryInsert(
                    "INSERT INTO `splashes` SET `discord_id` = ?, `user_id` = ?, `message_id` = ?, `splash_at` = ?",
                    channel.getGuild().getIdLong(),
                    author.getIdLong(),
                    message.getIdLong(),
                    time
                );

                if (!isNow) {
                    splashes.add(new SplashContainer(
                        channel.getGuild().getIdLong(),
                        author.getIdLong(),
                        message.getIdLong(),
                        time, note
                    ));
                }

                future.complete(null);
            } catch (SQLException throwable) {
                future.completeExceptionally(throwable);
            }
        }, future::completeExceptionally);

        return future;
    }

    private Message buildSplashMessage(User author, Carbon time, String note) {
        String description = ":user is splashing :time!";
        if (note != null && note.trim().length() > 0) {
            description += "\n\n> :note";
        }

        PlaceholderMessage embedMessage = MessageFactory.makeEmbeddedMessage(null)
            .setTimestamp(time.getTime().toInstant())
            .setDescription(description)
            .set("user", author.getAsMention())
            .set("note", note)
            .set("time", time.diffInSeconds(Carbon.now()) > 5
                ? "in " + time.diffForHumans()
                : "now"
            );

        MessageBuilder builder = new MessageBuilder()
            .setEmbed(embedMessage.buildEmbed());

        if (time.diffInSeconds(Carbon.now()) <= 300) {
            builder.setContent("@everyone");
        }

        return builder.build();
    }
}
