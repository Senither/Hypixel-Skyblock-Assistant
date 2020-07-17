package com.senither.hypixel.contracts.commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.SkyblockAssistant;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public abstract class Middleware {

    public static final Cache<Long, Boolean> messageCache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(2500, TimeUnit.MILLISECONDS)
        .build();

    public abstract boolean handle(@Nonnull SkyblockAssistant app, @Nonnull MessageReceivedEvent event, @Nonnull Command command) throws Exception;

    protected boolean runMessageCheck(long userId, @Nonnull Callable<Boolean> callback) throws Exception {
        return messageCache.get(userId, callback);
    }
}
