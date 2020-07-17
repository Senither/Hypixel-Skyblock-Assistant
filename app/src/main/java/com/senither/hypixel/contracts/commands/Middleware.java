package com.senither.hypixel.contracts.commands;

import com.senither.hypixel.SkyblockAssistant;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public interface Middleware {

    boolean handle(@Nonnull SkyblockAssistant app, @Nonnull MessageReceivedEvent event, @Nonnull Command command) throws Exception;
}
