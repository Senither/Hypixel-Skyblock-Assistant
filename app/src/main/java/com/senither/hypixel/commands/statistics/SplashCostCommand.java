package com.senither.hypixel.commands.statistics;

import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.contracts.commands.Command;
import com.senither.hypixel.hypixel.bazaar.BazaarProduct;
import com.senither.hypixel.hypixel.bazaar.BazaarProductReply;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SplashCostCommand extends Command {

    @SuppressWarnings("FieldCanBeLocal")
    private final double brewCosts = 0D +
        1000 +     // Tutti-Frutti Flavored Poison
        1500 +     // KnockOff Cola
        5000 * 4 + // Decent Coffee x4 (For Speed, Rabbit, Agility, and Adrenaline
        10000 +    // Slayer Energy Drink
        15000;     // Viking's Tear

    @SuppressWarnings("FieldCanBeLocal")
    private final double additionalCosts = 0D +
        1000 +      // Spirit Potion
        10000 +     // Magic Find Potion
        25000 +     // True Essence
        50000 * 3;  // Skill Potions x3

    private final List<BazaarProduct> products = Arrays.asList(
        BazaarProduct.ENCHANTED_REDSTONE_BLOCK,
        BazaarProduct.ENCHANTED_REDSTONE_LAMP,
        BazaarProduct.ENCHANTED_GUNPOWDER,

        BazaarProduct.ENCHANTED_BLAZE_ROD,
        BazaarProduct.ENCHANTED_SUGAR_CANE,
        BazaarProduct.ENCHANTED_COOKIE,

        BazaarProduct.ENCHANTED_CAKE,
        BazaarProduct.ENCHANTED_RABBIT_FOOT,
        BazaarProduct.ENCHANTED_RABBIT_HIDE
    );

    public SplashCostCommand(SkyblockAssistant app) {
        super(app);
    }

    @Override
    public String getName() {
        return "Splash Cost";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to calculate the average price for a 17 potion splash using the Bazaar prices.",
            "The calculation assumes you're splashing a combat XP potion plus two additional skill potions,",
            "as-well-as True Resistance 4, Dodge, 4, and Haste 3."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Displays the average cost for a single 17 pot splash.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("splash-cost", "splashcost", "sc");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {
        BazaarProductReply bazaarProducts = app.getHypixel().getBazaarProducts();
        if (bazaarProducts == null || !bazaarProducts.isSuccess()) {
            MessageFactory.makeError(event.getMessage(),
                "Failed to fetch the Bazaar product details, try again later."
            ).queue();
            return;
        }

        double totalCost = 0D;
        PlaceholderMessage message = MessageFactory.makeInfo(event.getMessage(),
            "A **17** potion splash costs **:price** coins on average with current Bazaar prices."
        ).setFooter(
            "The splash calculation assumes you're splashing all the normal buff potions(Strength 8, Critical 4, Archery 4, etc) using the special brews for the ones that requires it + 3 skill potions."
        );

        for (BazaarProduct bazaarProduct : products) {
            BazaarProductReply.Product product = bazaarProducts.getProduct(bazaarProduct);

            double price = product.getQuickStatus().getBuyPrice();
            totalCost += products.indexOf(bazaarProduct) < 3 ? price : price / 3D;

            message.addField(createEmbedField(bazaarProduct, product));
        }

        double redstoneLamp = bazaarProducts.getProduct(BazaarProduct.ENCHANTED_REDSTONE_LAMP).getQuickStatus().getBuyPrice();
        totalCost += ((redstoneLamp * 15) / 3) - redstoneLamp;

        double redstoneBlock = bazaarProducts.getProduct(BazaarProduct.ENCHANTED_REDSTONE_BLOCK).getQuickStatus().getBuyPrice();
        totalCost += ((redstoneBlock * 17) / 3) - redstoneBlock;

        double gunpowder = bazaarProducts.getProduct(BazaarProduct.ENCHANTED_GUNPOWDER).getQuickStatus().getBuyPrice();
        totalCost += ((gunpowder * 17) / 3) - gunpowder;

        message.set("price", NumberUtil.formatNicely(totalCost + brewCosts + additionalCosts)).queue();
    }

    private MessageEmbed.Field createEmbedField(BazaarProduct bazaarProduct, BazaarProductReply.Product product) {
        return new MessageEmbed.Field(
            bazaarProduct.getName(),
            NumberUtil.formatNicely(product.getQuickStatus().getBuyPrice()),
            true
        );
    }
}
