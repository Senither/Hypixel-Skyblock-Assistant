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

package com.senither.hypixel.rank.handler;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.contracts.rank.ItemRequirement;
import com.senither.hypixel.contracts.rank.ObjectClosure;
import com.senither.hypixel.contracts.rank.RankCommandHandler;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.exceptions.FriendlyException;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class ItemValueHandler extends RankCommandHandler {

    private final IntegerValueHandler pointHandler;
    private final Function<GuildController.GuildEntry.RankRequirement, HashMap<String, Integer>> getter;
    private final ItemRequirement[] items;

    public ItemValueHandler(ObjectClosure<Integer> closure, Function<GuildController.GuildEntry.RankRequirement, HashMap<String, Integer>> getter, ItemRequirement[] items) {
        this.pointHandler = new IntegerValueHandler(closure);
        this.getter = getter;
        this.items = items;
    }

    @Override
    public void handle(SkyblockAssistant app, MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply.Guild.Rank rank, String[] args) {
        if (args.length == 0) {
            throw new FriendlyException(
                "Missing option given for setting %s, you must either use `points` or `item`",
                rankType.getName()
            );
        }

        switch (args[0].toLowerCase()) {
            case "point":
            case "points":
                pointHandler.setRankType(rankType);
                pointHandler.handle(app, event, guildEntry, rank, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "gear":
            case "item":
            case "items":
                handleItem(app, event, guildEntry, rank, Arrays.copyOfRange(args, 1, args.length));
                break;

            default:
                throw new FriendlyException(String.format(
                    "Invalid option given for setting %s, you must either use `points` or `item`",
                    rankType.getName()
                ));
        }
    }

    private void handleItem(SkyblockAssistant app, MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply.Guild.Rank rank, String[] args) {
        if (args.length == 0) {
            throw new FriendlyException(
                "You must provide the name of the %s you want to use.",
                rankType.getName()
            );
        }

        int points = -1;
        String itemName = String.join(" ", args).toLowerCase();

        if (args.length > 1 && NumberUtil.isNumeric(args[args.length - 1])) {
            itemName = String.join(" ", Arrays.copyOfRange(
                args, 0, args.length - 1
            )).toLowerCase();
            points = NumberUtil.parseInt(args[args.length - 1], 1);
        }

        ItemRequirement selectedItem = null;
        for (ItemRequirement item : items) {
            if (item.getAliases().contains(itemName) || item.getName().equalsIgnoreCase(itemName)) {
                selectedItem = item;
                break;
            }
        }

        if (selectedItem == null) {
            throw new FriendlyException(
                "Unrecognized %s, `%s` is not a valid %s!",
                rankType.getName(), itemName, rankType.getName()
            );
        }

        HashMap<String, Integer> storedItems = getter.apply(getRequirementsForRank(guildEntry, rank));

        if (points < 0) {
            storedItems.remove(selectedItem.getName());
        } else {
            storedItems.put(selectedItem.getName(), points);
        }

        updateGuildEntry(app, event, guildEntry);

        MessageFactory.makeSuccess(event.getMessage(), points < 0
            ? "The **:name** :type was removed from the :type requirement for **:rank**!"
            : "The **:name** :type has been added for the **:rank** rank, It's worth **:points**"
        )
            .set("type", rankType.getName())
            .set("rank", rank.getName())
            .set("name", selectedItem.getName())
            .set("points", points)
            .queue();
    }
}
