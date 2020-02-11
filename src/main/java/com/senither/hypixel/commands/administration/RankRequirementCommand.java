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

package com.senither.hypixel.commands.administration;

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.database.controller.GuildController;
import com.senither.hypixel.rank.RankRequirementType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.hypixel.api.reply.GuildReply;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RankRequirementCommand extends Command {

    public RankRequirementCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Rank Requirement";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList();
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList();
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList();
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("rank-requirement");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        GuildController.GuildEntry guildEntry = GuildController.getGuildById(app.getDatabaseManager(), event.getGuild().getIdLong());
        if (guildEntry == null) {
            MessageFactory.makeError(event.getMessage(),
                "The server is not currently setup with a guild, you must setup "
                    + "the server with a guild before you can use this command!"
            ).setTitle("Server is not setup").queue();
            return;
        }

        if (!isGuildMasterOrOfficerOfServerGuild(event, guildEntry)) {
            MessageFactory.makeError(event.getMessage(),
                "You must be the guild master or an officer of the **:name** guild to use this command!"
            ).set("name", guildEntry.getName()).setTitle("Missing permissions").queue();
            return;
        }

        GuildReply guildReply = app.getHypixel().getGson().fromJson(guildEntry.getData(), GuildReply.class);

        if (args.length == 0) {
            sendListOfGuildRanks(event, guildEntry, guildReply);
            return;
        }

        GuildReply.Guild.Rank rank = null;
        for (GuildReply.Guild.Rank guildRank : guildReply.getGuild().getRanks()) {
            if (guildRank.getName().equalsIgnoreCase(args[0])) {
                rank = guildRank;
                break;
            }
        }

        if (rank == null) {
            MessageFactory.makeWarning(event.getMessage(),
                "You must mention a valid Guild rank to use this command!"
            ).setTitle("Invalid Guild Rank").queue();
            return;
        }

        if (args.length == 1) {
            sendRankRequirements(event, guildEntry, guildReply, rank);
            return;
        }

        RankRequirementType action = null;
        switch (args[1].toLowerCase()) {
            case "reset":
            case "delete":
                resetRankOption(event, guildEntry, rank);
                return;

            case "skill":
            case "skills":
                action = RankRequirementType.AVERAGE_SKILLS;
                break;

            case "fairy":
            case "soul":
            case "souls":
                action = RankRequirementType.FAIRY_SOULS;
                break;

            case "slayer":
            case "slayers":
                action = RankRequirementType.SLAYER;
                break;

            case "armor":
            case "armors":
                action = RankRequirementType.ARMOR;
                break;

            case "weapon":
            case "weapons":
                action = RankRequirementType.WEAPONS;
                break;

            case "talis":
            case "talisman":
            case "talismans":
                action = RankRequirementType.TALISMANS;
                break;

            case "orb":
            case "orbs":
            case "powerorb":
                action = RankRequirementType.POWER_ORBS;
                break;
        }

        if (action == null) {
            MessageFactory.makeWarning(event.getMessage(), "Invalid role option, `:value` is not a valid option!")
                .set("value", args[0])
                .setTitle("Invalid Option")
                .queue();
            return;
        }

        action.getHandler().handle(app, event, guildEntry, rank, Arrays.copyOfRange(args, 2, args.length));
    }

    private void sendListOfGuildRanks(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply guildReply) {
        PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(),
            "\uD83D\uDEE0 Has requirements setup \uD83D\uDD27 Doesn't have requirements setup\n\n:note"
        ).setTitle("Rank Requirements");

        List<String> roles = new ArrayList<>();
        guildReply.getGuild().getRanks()
            .stream().sorted((o1, o2) -> o2.getPriority() - o1.getPriority())
            .forEach(rank -> {
                roles.add(String.format("%s %s",
                    guildEntry.getRankRequirements().containsKey(rank.getName())
                        ? "\uD83D\uDEE0"
                        : "\uD83D\uDD27",
                    rank.getName()
                ));
            });

        message.set("note", String.join("\n", roles))
            .setFooter(String.format("You can use \"%srank-requirement <role> <option>\" to setup requirements",
                Constants.COMMAND_PREFIX
            ))
            .queue();
    }

    private void sendRankRequirements(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply guildReply, GuildReply.Guild.Rank rank) {
        GuildController.GuildEntry.RankRequirement requirement = guildEntry.getRankRequirements().get(rank.getName());
        if (requirement == null) {
            MessageFactory.makeWarning(event.getMessage(),
                "There are no rank requirements setup for the **:name** rank!"
            ).set("name", rank.getName()).queue();
            return;
        }

        PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(), "")
            .setTitle("Rank Requirements for " + rank.getName());

        for (RankRequirementType requirementType : RankRequirementType.values()) {
            try {
                message.addField(
                    requirementType.getName(),
                    requirementType.getChecker().getRankRequirementNote(requirement),
                    false
                );
            } catch (Exception e) {
            }
        }

        message.queue();
    }

    private void resetRankOption(MessageReceivedEvent event, GuildController.GuildEntry guildEntry, GuildReply.Guild.Rank rank) {
        guildEntry.getRankRequirements().remove(rank.getName());
        updateGuildEntry(event, guildEntry);

        MessageFactory.makeSuccess(event.getMessage(), "All the rank requirements for the **:name** role have been reset!")
            .set("name", rank.getName())
            .queue();
    }

    private void updateGuildEntry(MessageReceivedEvent event, GuildController.GuildEntry guildEntry) {
        try {
            app.getDatabaseManager().queryUpdate("UPDATE `guilds` SET `rank_requirements` = ? WHERE `discord_id` = ?",
                app.getHypixel().getGson().toJson(guildEntry.getRankRequirements()), event.getGuild().getIdLong()
            );

            GuildController.forgetCacheFor(event.getGuild().getIdLong());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
