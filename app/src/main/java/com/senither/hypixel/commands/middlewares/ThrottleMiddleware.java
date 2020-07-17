package com.senither.hypixel.commands.middlewares;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.commands.ThrottleContainer;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.contracts.commands.Middleware;
import com.senither.hypixel.metrics.Metrics;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class ThrottleMiddleware extends Middleware {

    public static final Cache<Long, ThrottleEntity> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build();

    @Override
    public boolean handle(@Nonnull SkyblockAssistant app, @Nonnull MessageReceivedEvent event, @Nonnull Command command) throws Exception {
        ThrottleContainer throttleContainer = command.getThrottleContainer();
        ThrottleEntity entity = getEntityFromCache(event.getAuthor().getIdLong(), throttleContainer);

        if (entity.getHits() >= throttleContainer.getMaxAttempts()) {
            return cancelCommandThrottleRequest(event, command, entity);
        }

        entity.incrementHit();

        return true;
    }

    private boolean cancelCommandThrottleRequest(MessageReceivedEvent event, Command command, ThrottleEntity entity) throws Exception {
        Metrics.commandsRatelimited.labels(command.getClass().getSimpleName()).inc();

        return runMessageCheck(event.getAuthor().getIdLong(), () -> {
            MessageFactory.makeWarning(event.getMessage(), "Too many `:command` attempts. Please try again in **:time** seconds.")
                .set("command", command.getName())
                .set("time", ((entity.getTime() - System.currentTimeMillis()) / 1000) + 1)
                .queue(newMessage -> newMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, throwable -> {
                }));

            return false;
        });
    }

    private ThrottleEntity getEntityFromCache(long fingerprint, ThrottleContainer container) throws Exception {
        ThrottleEntity entity = cache.get(fingerprint, () -> new ThrottleEntity(container));

        if (entity.hasExpired()) {
            cache.invalidate(fingerprint);
            return getEntityFromCache(fingerprint, container);
        }

        return entity;
    }

    private static class ThrottleEntity {

        private final int maxAttempts;
        private final long time;
        private int hit;

        public ThrottleEntity(ThrottleContainer container) {
            this.time = System.currentTimeMillis() + (container.getDecaySeconds() * 1000);
            this.maxAttempts = container.getMaxAttempts();
            this.hit = 0;
        }

        public int getHits() {
            return hit;
        }

        public void incrementHit() {
            hit++;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public long getTime() {
            return time;
        }

        public boolean hasExpired() {
            return System.currentTimeMillis() > time;
        }
    }
}
