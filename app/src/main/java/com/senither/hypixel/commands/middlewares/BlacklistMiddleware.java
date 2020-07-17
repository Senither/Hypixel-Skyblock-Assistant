package com.senither.hypixel.commands.middlewares;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.commands.Middleware;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class BlacklistMiddleware extends Middleware {

    @Override
    public boolean handle(@Nonnull SkyblockAssistant app, @Nonnull MessageReceivedEvent event, @Nonnull Command command) throws Exception {
        return !app.getBlacklist().isBlacklisted(event.getAuthor());
    }
}
