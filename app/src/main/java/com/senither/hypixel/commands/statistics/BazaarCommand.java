package com.senither.hypixel.commands.statistics;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.hypixel.bazaar.BazaarProduct;
import com.senither.hypixel.hypixel.bazaar.BazaarProductReply;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BazaarCommand extends Command {

    public BazaarCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Bazaar Command";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "List and calculates the price of items that can be found on the Bazaar,",
            "multiple items can also be calculated at the same time, up to a",
            "maximum of six items."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [amount] <item>` - Gets the price for the given item.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 900 summoning eye` - Gets the price of 900 Summoning Eyes.",
            "`:command e redstone block + e redstone lamp` - Gets the price of Enchanted Redstone Blocks and Lamps.",
            "`:command 64 e gold block + 32 e diamond block` - Gets the price of a stack of enchanted gold blocks, and 32 enchanted diamond blocks."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("bazaar", "bz");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeError(event.getMessage(),
                "You must include the Bazaar product you want to see the sell and buy prices for."
            ).queue();
            return;
        }

        BazaarProductReply bazaarProducts = app.getHypixel().getBazaarProducts();
        if (bazaarProducts == null || !bazaarProducts.isSuccess()) {
            MessageFactory.makeError(event.getMessage(),
                "Failed to fetch the Bazaar product details, try again later."
            ).queue();
            return;
        }

        String[] parts = String.join(" ", args).split("\\+");
        if (parts.length > 6) {
            MessageFactory.makeWarning(event.getMessage(),
                "You can only calculate the price of 6 items at a time, please lower the number of items you want to see the price for."
            ).queue();
            return;
        }

        double totalBuy = 0D;
        double totalSell = 0D;

        PlaceholderMessage placeholderMessage = MessageFactory.makeInfo(event.getMessage(), "")
            .setTitle("Bazaar Summary");

        for (String part : String.join(" ", args).split("\\+")) {
            int amount = 1;
            String[] split = part.trim().split(" ");

            if (NumberUtil.isNumeric(split[0])) {
                amount = NumberUtil.getBetween(NumberUtil.parseInt(split[0], 1), 1, 71680);
                split = Arrays.copyOfRange(split, 1, split.length);
            }

            BazaarProduct bazaarProduct = BazaarProduct.getFromName(String.join(" ", split));
            if (bazaarProduct == null) {
                MessageFactory.makeWarning(event.getMessage(),
                    "Find to find any Bazaar product with the name of **:name**, "
                        + "please make sure the product you're trying to view actually exists, or is spelled corrected."
                ).set("name", String.join(" ", split)).queue();
                return;
            }

            BazaarProductReply.Product product = bazaarProducts.getProduct(bazaarProduct);

            double buyPrice = product.getQuickStatus().getBuyPrice();
            double sellPrice = product.getQuickStatus().getSellPrice();

            totalBuy += buyPrice * amount;
            totalSell += sellPrice * amount;

            placeholderMessage.addField(
                String.format("%sx %s", NumberUtil.formatNicely(amount), bazaarProduct.getName()),
                createItemDescription(buyPrice, sellPrice, amount),
                false
            );
        }

        placeholderMessage.addField("Summary",
            String.format("```yml\nTotal Buy Price:  %s\nTotal Sell Price: %s```",
                NumberUtil.formatNicelyWithDecimals(totalBuy),
                NumberUtil.formatNicelyWithDecimals(totalSell)
            ), false
        );

        placeholderMessage.queue();
    }

    private String createItemDescription(double buyPrice, double sellPrice, int amount) {
        String totalSellPriceString = String.format("Sell %s Price: ", NumberUtil.formatNicely(amount));

        List<String> content = new ArrayList<>(Arrays.asList(
            padSpaces("Buy 1 Price:", totalSellPriceString.length()) + NumberUtil.formatNicelyWithDecimals(buyPrice),
            padSpaces("Sell 1 Price:", totalSellPriceString.length()) + NumberUtil.formatNicelyWithDecimals(sellPrice)
        ));

        if (amount > 1) {
            content.add(
                padSpaces(String.format("Buy %s Price:", NumberUtil.formatNicely(amount)), totalSellPriceString.length())
                    + NumberUtil.formatNicelyWithDecimals(buyPrice * amount)
            );

            content.add(totalSellPriceString + NumberUtil.formatNicelyWithDecimals(sellPrice * amount));
        }

        return String.format("```yml\n%s```", String.join("\n", content));
    }

    private String padSpaces(String string, double size) {
        StringBuilder builder = new StringBuilder(string);
        while (builder.length() < size) {
            builder.append(" ");
        }
        return builder.toString();
    }
}
