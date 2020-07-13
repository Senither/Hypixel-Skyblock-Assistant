package com.senither.hypixel.listeners;

import com.senither.hypixel.metrics.Metrics;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class GenericEventListener extends ListenerAdapter {

    @Override
    public void onGenericEvent(@Nonnull GenericEvent event) {
        Metrics.jdaEvents.labels(event.getClass().getSimpleName()).inc();
    }
}
