package com.senither.hypixel.commands.middlewares;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.commands.Middleware;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class VerificationMiddleware extends Middleware {

    public static final Cache<Long, Boolean> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .recordStats()
        .build();

    public static void clearVerificationCacheFor(User user) {
        cache.invalidate(user.getIdLong());
    }

    @Override
    public boolean handle(@Nonnull SkyblockAssistant app, @Nonnull MessageReceivedEvent event, @Nonnull Command command) throws Exception {
        if (!command.isVerificationRequired() || isUserVerified(app, event.getAuthor())) {
            return true;
        }

        MessageFactory.makeError(event.getMessage(), String.join("\n", Arrays.asList(
            "You must verify your account with the bot to use this command, you can do this by",
            "running `:prefixverify <username>`, where your username is your in-game Minecraft",
            "username that has your Discord account linked on Hypixel.net",
            "",
            "If you haven't already linked your Discord account on Hypixel you can login to",
            "the server, go to your Hypixel social settings, click on Discord, and set",
            "your username to `:user`"
        )))
            .set("prefix", Constants.COMMAND_PREFIX)
            .set("user", event.getAuthor().getAsTag())
            .setTitle("Missing verification")
            .queue();

        return false;
    }

    private boolean isUserVerified(SkyblockAssistant app, User user) {
        Boolean verificationState = cache.getIfPresent(user.getIdLong());
        if (verificationState != null) {
            return verificationState;
        }

        try {
            boolean result = app.getHypixel().getUUIDFromUser(user) != null;

            cache.put(user.getIdLong(), result);
            return result;
        } catch (SQLException ignored) {
            return false;
        }
    }
}

