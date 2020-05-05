/*
 * Copyright (c) 2020.
 *
 * This file is part of Hypixel Skyblock Assistant.
 *
 * Hypixel Guild Synchronizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypixel Guild Synchronizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hypixel Guild Synchronizer.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.senither.hypixel.listeners;

import com.senither.hypixel.commands.administration.DonationCommand;
import com.senither.hypixel.contracts.commands.DonationAdditionFunction;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ReactionEventListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (isCheckmarkEmote(event) && DonationCommand.confirmationQueue.containsKey(event.getMessageIdLong())) {
            DonationAdditionFunction donationAdditionFunction = DonationCommand.confirmationQueue.get(event.getMessageIdLong());

            if (event.getUserIdLong() == donationAdditionFunction.getAuthorId()) {
                donationAdditionFunction.handle();

                DonationCommand.confirmationQueue.remove(event.getMessageIdLong());
                event.getTextChannel().deleteMessageById(event.getMessageIdLong()).queue();
            }
        }
    }

    private boolean isCheckmarkEmote(MessageReactionAddEvent event) {
        try {
            return Objects.equals(event.getReactionEmote().getEmoji(), "✅");
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
