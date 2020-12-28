package com.senither.hypixel.commands.statistics;

import com.senither.hypixel.Constants;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.chat.SimplePaginator;
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.hypixel.response.AuctionHouseResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AuctionHouseCommand extends SkillCommand {

    private static final Logger log = LoggerFactory.getLogger(AuctionHouseCommand.class);

    public AuctionHouseCommand(SkyblockAssistant app) {
        super(app, "Auction");
    }

    @Override
    public String getName() {
        return "Auction House";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets a list of items on a users auction house, including total",
            "amount of bids, the highest bid, what the item ended up being",
            "sold for, and a view of unclaimed items and their total",
            "accumulated value."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username>` - Gets AH info for the given username",
            "`:command <mention>` - Gets AH info for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("auctions", "auction", "ah");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, String[] args) {
        AuctionHouseResponse auctions = app.getHypixel().getAuctionsFromProfile(
            profileReply.getProfile().get("profile_id").getAsString()
        );

        if (auctions == null) {
            auctions = app.getHypixel().getAuctionsFromProfile(
                profileReply.getProfile().get("profile_id").getAsString()
            );
        }

        if (auctions == null) {
            MessageFactory.makeError(message,
                "Failed to load your auction information, please try again later!"
            ).queue();
            return;
        }

        if (auctions.getAuctions().isEmpty()) {
            sendNoAuctionsMessage(message, playerReply, profileReply);
            return;
        }

        long totalUnclaimedAuctions = auctions.getAuctions()
            .stream().filter(auction -> !auction.isClaimed())
            .count();

        if (totalUnclaimedAuctions == 0) {
            sendNoAuctionsMessage(message, playerReply, profileReply);
            return;
        }

        List<MessageEmbed.Field> messages = new ArrayList<>();
        auctions.getAuctions().stream()
            .filter(auction -> !auction.isClaimed())
            .sorted(Comparator.comparingLong(AuctionHouseResponse.Auction::getEnd))
            .forEachOrdered(auction -> {
                String time = Carbon.now().addSeconds(
                    Math.toIntExact((auction.getEnd() - System.currentTimeMillis()) / 1000)
                ).diffForHumans(true);

                String fieldMessage;

                // If the auction has ended, we'll use this message.
                if (auction.getEnd() <= System.currentTimeMillis()) {
                    fieldMessage = String.format(
                        "Sold for:     %s\n- This auction has already ended!",
                        NumberUtil.formatNicely(auction.getHighestBidAmount()),
                        time
                    );
                }
                // If the auction has no bids
                else if (auction.getBids().isEmpty()) {
                    fieldMessage = String.format(
                        "Starting Bid: %s\nEnds in:      %s",
                        NumberUtil.formatNicely(auction.getStartingBid()),
                        time
                    );
                }
                // If the auction has not yet ended and has at least one bid
                else {
                    fieldMessage = String.format(
                        "Current Bid:  %s\nTotal Bids:   %s\nEnds in:      %s",
                        NumberUtil.formatNicely(auction.getHighestBidAmount()),
                        NumberUtil.formatNicely(auction.getBids().size()),
                        time
                    );
                }

                messages.add(new MessageEmbed.Field(
                    auction.getItemName(),
                    String.format("```yml\n%s```", fieldMessage),
                    false
                ));
            });

        int currentPage = 1;
        if (args.length > 0) {
            currentPage = NumberUtil.parseInt(args[0], 1);
        }

        long totalUnclaimedCoins = auctions.getAuctions().stream()
            .filter(auction -> !auction.isClaimed())
            .filter(auction -> !auction.getBids().isEmpty())
            .mapToLong(AuctionHouseResponse.Auction::getHighestBidAmount)
            .sum();

        String displayName = playerReply.getPlayer().get("displayname").getAsString();

        PlaceholderMessage placeholderMessage = MessageFactory.makeInfo(message,
            "**:name** has **:amount** unclaimed coins on their auction."
        ).set("name", displayName).set("amount", NumberUtil.formatNicelyWithDecimals(totalUnclaimedCoins))
            .setTitle(String.format("%s's Auctions", displayName))
            .setFooter(String.format(
                "Note > Auctions only update once a minute | Profile: %s",
                profileReply.getProfile().get("cute_name").getAsString()
            ));

        SimplePaginator<MessageEmbed.Field> paginator = new SimplePaginator<>(messages, 5, currentPage);

        paginator.forEach((index, key, val) -> placeholderMessage.addField(val));

        placeholderMessage.addField("", paginator.generateFooter(
            Constants.COMMAND_PREFIX + getTriggers().get(0) + " " + displayName
        ), false);

        message.editMessage(placeholderMessage.buildEmbed()).queue();
    }

    private void sendNoAuctionsMessage(Message message, PlayerReply playerReply, SkyBlockProfileReply profileReply) {
        message.editMessage(MessageFactory.makeWarning(message,
            "**:name** has no active or unclaimed auctions on their **:profile** profile."
            )
                .setTitle(String.format("%s's Auctions",
                    playerReply.getPlayer().get("displayname").getAsString()
                ))
                .set("name", playerReply.getPlayer().get("displayname").getAsString())
                .set("profile", profileReply.getProfile().get("cute_name").getAsString())
                .buildEmbed()
        ).queue();
    }
}
