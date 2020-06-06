package com.senither.hypixel.splash;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.database.collection.Collection;
import com.senither.hypixel.database.collection.DataRow;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SplashManager {

    private static final Logger log = LoggerFactory.getLogger(SplashManager.class);
    private static final int endingSoonTimer = 300;

    private final SkyblockAssistant app;
    private final Set<SplashContainer> splashes;

    public SplashManager(SkyblockAssistant app) {
        this.app = app;
        this.splashes = new HashSet<>();

        try {
            for (DataRow row : app.getDatabaseManager().query("SELECT * FROM `splashes` WHERE `splash_at` > ?", Carbon.now())) {
                splashes.add(new SplashContainer(
                    row.getLong("id"),
                    row.getLong("discord_id"),
                    UUID.fromString(row.getString("uuid")),
                    row.getLong("message_id"),
                    row.getTimestamp("splash_at"),
                    row.getString("note")
                ));
            }
        } catch (SQLException e) {
            log.error("A SQL exception were thrown while loading splashes from the database, error: {}", e.getMessage(), e);
        }
    }

    public static int getEndingSoonTimer() {
        return endingSoonTimer;
    }

    public Set<SplashContainer> getSplashes() {
        return splashes;
    }

    public List<SplashContainer> getSplashesForGuildById(long guildId) {
        if (getSplashes().isEmpty()) {
            return new ArrayList<>();
        }

        List<SplashContainer> splashes = new ArrayList<>();
        for (SplashContainer splash : getSplashes()) {
            if (splash.getDiscordId() == guildId) {
                splashes.add(splash);
            }
        }

        return splashes.stream()
            .sorted(Comparator.comparingLong(o -> o.getTime().getTimestamp()))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public SplashContainer getPendingSplashById(long id) {
        for (SplashContainer splash : getSplashes()) {
            if (splash.getId() == id) {
                return splash;
            }
        }
        return null;
    }

    public SplashContainer getSplashById(int splashId) throws SQLException {
        SplashContainer pendingSplashById = getPendingSplashById(splashId);
        if (pendingSplashById != null) {
            return pendingSplashById;
        }

        Collection result = app.getDatabaseManager().query("SELECT * FROM `splashes` WHERE `id` = ?", splashId);
        if (result.isEmpty()) {
            return null;
        }

        DataRow row = result.first();

        return new SplashContainer(
            row.getLong("id"),
            row.getLong("discord_id"),
            UUID.fromString(row.getString("uuid")),
            row.getLong("message_id"),
            row.getTimestamp("splash_at"),
            row.getString("note")
        );
    }

    public SplashContainer getEarliestSplashFromUser(UUID userUuid) {
        return getSplashes().stream().filter(splashContainer -> {
            return splashContainer.getUserUuid().equals(userUuid);
        }).min((o1, o2) -> {
            return Math.toIntExact(o1.getTime().getTimestamp() - o2.getTime().getTimestamp());
        }).orElse(null);
    }

    public void updateSplashFor(SplashContainer splash) {
        GuildController.GuildEntry guild = GuildController.getGuildById(app.getDatabaseManager(), splash.getDiscordId());
        if (guild == null || !guild.isSplashTrackerEnabled()) {
            return;
        }

        Long userDiscordId = app.getHypixel().getDiscordIdFromUUID(splash.getUserUuid());
        if (userDiscordId == null) {
            return;
        }

        User userById = app.getShardManager().getUserById(userDiscordId);
        if (userById == null) {
            return;
        }

        TextChannel channelById = app.getShardManager().getTextChannelById(guild.getSplashChannel());
        if (channelById == null) {
            return;
        }

        if (splash.isEndingSoon() && splash.getTime().getTimestamp() - splash.getLastUpdatedAt() > endingSoonTimer) {
            channelById.deleteMessageById(splash.getMessageId()).queue(null, null);

            channelById.sendMessage(buildSplashMessage(
                userById, splash.getTime(), splash.getNote(), splash.getId()
            )).queue(message -> {
                try {
                    splash.setMessageId(message.getIdLong());

                    app.getDatabaseManager().queryUpdate("UPDATE `splashes` SET `message_id` = ? WHERE `discord_id` = ? and `message_id` = ?",
                        message.getIdLong(), splash.getDiscordId(), splash.getMessageId()
                    );
                } catch (SQLException e) {
                    log.error("Something went wrong while trying to send \"ending soon\" splash message, error: {}", e.getMessage(), e);
                }
            });
        } else {
            channelById.editMessageById(splash.getMessageId(), buildSplashMessage(
                userById, splash.getTime(), splash.getNote(), splash.getId()
            )).queue(null, null);
        }
    }

    public CompletableFuture<Boolean> removeSplashById(SplashContainer splash) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            getSplashes().removeIf(next -> next.getId() == splash.getId());
            app.getDatabaseManager().queryUpdate("DELETE FROM `splashes` WHERE `id` = ?",
                splash.getId()
            );

            GuildController.GuildEntry guild = GuildController.getGuildById(app.getDatabaseManager(), splash.getDiscordId());
            if (guild == null || !guild.isSplashTrackerEnabled()) {
                future.complete(false);
                return future;
            }

            TextChannel channelById = app.getShardManager().getTextChannelById(guild.getSplashChannel());
            if (channelById == null) {
                future.complete(false);
                return future;
            }

            channelById.editMessageById(splash.getMessageId(), MessageFactory.makeEmbeddedMessage(null)
                .setColor(MessageType.WARNING.getColor())
                .setTimestamp(Carbon.now().getTime().toInstant())
                .setDescription("This splash has been canceled.")
                .buildEmbed()
            ).queue(message -> {
                future.complete(true);
                message.delete().queueAfter(30, TimeUnit.SECONDS);
            }, error -> future.complete(false));
        } catch (SQLException e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<Boolean> cancelSplash(SplashContainer splash) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        GuildController.GuildEntry guild = GuildController.getGuildById(app.getDatabaseManager(), splash.getDiscordId());
        if (guild == null || !guild.isSplashTrackerEnabled()) {
            future.completeExceptionally(new FriendlyException("Failed to load guild entry for the server."));
            return future;
        }

        TextChannel channelById = app.getShardManager().getTextChannelById(guild.getSplashChannel());
        if (channelById == null) {
            future.completeExceptionally(new FriendlyException("Failed to find splash channel, has the feature been enabled?"));
            return future;
        }

        channelById.editMessageById(splash.getMessageId(), MessageFactory.makeEmbeddedMessage(null)
            .setColor(MessageType.WARNING.getColor())
            .setTimestamp(Carbon.now().getTime().toInstant())
            .setDescription("This splash has been canceled.")
            .buildEmbed()
        ).queue(message -> {
            try {
                getSplashes().removeIf(next -> next.getId() == splash.getId());
                app.getDatabaseManager().queryUpdate("DELETE FROM `splashes` WHERE `id` = ?",
                    splash.getId()
                );

                future.complete(true);

                message.delete().queueAfter(30, TimeUnit.SECONDS);
            } catch (SQLException e) {
                log.error("Something went wrong while trying to send \"ending soon\" splash message, error: {}", e.getMessage(), e);

                future.completeExceptionally(e);
            }
        }, future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Long> createSplash(TextChannel channel, User author, Carbon time, String note) {
        CompletableFuture<Long> future = new CompletableFuture<>();

        final boolean isNow = time.diffInSeconds(Carbon.now()) <= 5;

        channel.sendMessage(buildSplashMessage(author, time, note, null)).queue(message -> {
            try {
                String encodedNote = "base64:" + new String(Base64.getEncoder().encode(note.getBytes()));
                Set<Long> ids = app.getDatabaseManager().queryInsert(
                    "INSERT INTO `splashes` SET `discord_id` = ?, `uuid` = ?, `message_id` = ?, `note` = ?, `splash_at` = ?",
                    channel.getGuild().getIdLong(),
                    app.getHypixel().getUUIDFromUser(author),
                    message.getIdLong(),
                    encodedNote,
                    time
                );

                Long splashEntryId = ids.iterator().next();

                if (!isNow) {
                    splashes.add(new SplashContainer(
                        splashEntryId,
                        channel.getGuild().getIdLong(),
                        app.getHypixel().getUUIDFromUser(author),
                        message.getIdLong(),
                        time, note
                    ));
                }

                message.editMessage(buildSplashMessage(author, time, note, splashEntryId)).queue();

                future.complete(splashEntryId);
            } catch (SQLException throwable) {
                future.completeExceptionally(throwable);
            }
        }, future::completeExceptionally);

        return future;
    }

    private Message buildSplashMessage(User author, Carbon time, String note, Long id) {
        String description = ":user (IGN: :name) is splashing :time!";
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

        try {
            final String username = app.getHypixel().getUsernameFromUuid(
                app.getHypixel().getUUIDFromUser(author)
            );

            embedMessage.set("name", username.startsWith("_") ? "\\" + username : username);
        } catch (SQLException e) {
            embedMessage.set("name", "_Unable to load_");
        }

        if (id != null) {
            embedMessage.setFooter("Splash ID: " + NumberUtil.formatNicely(id));
        }

        MessageBuilder builder = new MessageBuilder()
            .setEmbed(embedMessage.buildEmbed());

        if (time.diffInSeconds(Carbon.now()) <= endingSoonTimer) {
            builder.setContent("@everyone");
        }

        return builder.build();
    }
}
