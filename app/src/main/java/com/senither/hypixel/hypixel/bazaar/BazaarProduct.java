package com.senither.hypixel.hypixel.bazaar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum BazaarProduct {

    // Farming
    SEEDS("SEEDS", "Seeds", "Seed"),
    ENCHANTED_SEEDS("ENCHANTED_SEEDS", "Enchanted Seeds", "E Seeds", "E Seed"),
    HAY_BLOCK("HAY_BLOCK", "Hay Block", "hay"),
    ENCHANTED_HAY_BLOCK("ENCHANTED_HAY_BLOCK", "Enchanted Hay Block", "E Hay Block", "E Hay"),
    BROWN_MUSHROOM("BROWN_MUSHROOM", "Brown Mushroom"),
    RED_MUSHROOM("RED_MUSHROOM", "Red Mushroom"),
    ENCHANTED_RED_MUSHROOM("ENCHANTED_RED_MUSHROOM", "Enchanted Red Mushroom", "E Red Mushroom"),
    ENCHANTED_BROWN_MUSHROOM("ENCHANTED_BROWN_MUSHROOM", "Enchanted Brown Mushroom", "E Brown Mushroom"),
    BROWN_MUSHROOM_BLOCK("HUGE_MUSHROOM_1", "Brown Mushroom Block"),
    RED_MUSHROOM_BLOCK("HUGE_MUSHROOM_2", "Red Mushroom Block"),
    ENCHANTED_BROWN_MUSHROOM_BLOCK("ENCHANTED_HUGE_MUSHROOM_1", "Enchanted Brown Mushroom Block", "E Brown Mushroom Block"),
    ENCHANTED_RED_MUSHROOM_BLOCK("ENCHANTED_HUGE_MUSHROOM_2", "Enchanted Red Mushroom Block", "E Red Mushroom Block"),
    COCOA_BEANS("INK_SACK:3", "Coco Beans", "Coco Bean", "Coco"),
    ENCHANTED_COCOA("ENCHANTED_COCOA", "Enchanted Coco Bean", "E Coco Bean", "E Coco"),
    ENCHANTED_COOKIE("ENCHANTED_COOKIE", "Enchanted Cookie", "E Cookie"),
    CARROT_ITEM("CARROT_ITEM", "Carrot"),
    ENCHANTED_CARROT("ENCHANTED_CARROT", "Enchanted Carrot", "E Carrot"),
    ENCHANTED_CARROT_ON_A_STICK("ENCHANTED_CARROT_STICK", "Enchanted Carrot on a Stick", "E Carrot on a Stick", "E Carrot on Stick"),
    ENCHANTED_GOLDEN_CARROT("ENCHANTED_GOLDEN_CARROT", "Enchanted Golden Carrot", "E Golden Carrot"),
    POTATO_ITEM("POTATO_ITEM", "Potato"),
    ENCHANTED_POTATO("ENCHANTED_POTATO", "Enchanted Potato", "E Potato"),
    ENCHANTED_BAKED_POTATO("ENCHANTED_BAKED_POTATO", "Enchanted Baked Potato", "E baked potato"),
    RABBIT("RABBIT", "Rabbit"),
    ENCHANTED_RABBIT("ENCHANTED_RABBIT", "Enchanted Rabbit", "E Rabbit"),
    RABBIT_HIDE("RABBIT_HIDE", "Rabbit Hide"),
    ENCHANTED_RABBIT_FOOT("ENCHANTED_RABBIT_FOOT", "Enchanted Rabbit Foot", "E Rabbit Foot"),
    ENCHANTED_RABBIT_HIDE("ENCHANTED_RABBIT_HIDE", "Enchanted Rabbit Hide", "E Rabbit Hide"),
    MELON("MELON", "Melon"),
    ENCHANTED_MELON("ENCHANTED_MELON", "Enchanted Melon", "E Melon"),
    ENCHANTED_MELON_BLOCK("ENCHANTED_MELON_BLOCK", "Enchanted Melon Block", "E Melon Block"),
    ENCHANTED_GLISTERING_MELON("ENCHANTED_GLISTERING_MELON", "Enchanted Glistering Melon", "E Glistering Melon"),
    SUGAR_CANE("SUGAR_CANE", "Sugarcane"),
    ENCHANTED_PAPER("ENCHANTED_PAPER", "Enchanted Paper", "E Paper"),
    ENCHANTED_SUGAR("ENCHANTED_SUGAR", "Enchanted Sugar", "E Sugar"),
    ENCHANTED_SUGAR_CANE("ENCHANTED_SUGAR_CANE", "Enchanted Sugarcane", "E Sugarcane"),
    CACTUS("CACTUS", "Cactus"),
    ENCHANTED_CACTUS("ENCHANTED_CACTUS", "Enchanted Cactus", "E Cactus"),
    ENCHANTED_CACTUS_GREEN("ENCHANTED_CACTUS_GREEN", "Enchanted Cactus Green", "E Cactus Green"),
    ENCHANTED_EGG("ENCHANTED_EGG", "Enchanted Egg", "E Egg"),
    SUPER_EGG("SUPER_EGG", "Enchanted Super Egg", "E Super Egg"),
    ENCHANTED_CAKE("ENCHANTED_CAKE", "Enchanted Cake", "E Cake"),
    PUMPKIN("PUMPKIN", "Pumpkin"),
    ENCHANTED_PUMPKIN("ENCHANTED_PUMPKIN", "Enchanted Pumpkin", "E Pumpkin"),
    WHEAT("WHEAT", "Wheat"),
    ENCHANTED_BREAD("ENCHANTED_BREAD", "Enchanted Bread", "E Bread"),
    MUTTON("MUTTON", "Mutton"),
    ENCHANTED_MUTTON("ENCHANTED_MUTTON", "Enchanted Mutton", "E Mutton"),
    ENCHANTED_COOKED_MUTTON("ENCHANTED_COOKED_MUTTON", "Enchanted Cooked Mutton", "E Cooked Mutton"),
    RAW_BEEF("RAW_BEEF", "Raw Beef"),
    ENCHANTED_RAW_BEEF("ENCHANTED_RAW_BEEF", "Enchanted Raw Beef", "E Raw Beef"),
    PORK("PORK", "Pork"),
    ENCHANTED_PORK("ENCHANTED_PORK", "Enchanted Pork", "E Pork"),
    ENCHANTED_GRILLED_PORK("ENCHANTED_GRILLED_PORK", "Enchanted Grilled Pork", "E Grilled Pork"),
    RAW_CHICKEN("RAW_CHICKEN", "Raw Chicken"),
    ENCHANTED_RAW_CHICKEN("ENCHANTED_RAW_CHICKEN", "Enchanted Raw Chicken", "E Raw Chicken"),
    RABBIT_FOOT("RABBIT_FOOT", "Rabbit Foot"),
    LEATHER("LEATHER", "Leather"),
    ENCHANTED_LEATHER("ENCHANTED_LEATHER", "Enchanted Leather", "E Leather"),
    FEATHER("FEATHER", "Feather"),
    ENCHANTED_FEATHER("ENCHANTED_FEATHER", "Enchanted Feather", "E Feather"),

    // Mining,
    COBBLESTONE("COBBLESTONE", "Cobblestone", "Cobble"),
    ENCHANTED_COBBLESTONE("ENCHANTED_COBBLESTONE", "Enchanted Cobblestone", "E Cobblestone", "E Cobble"),
    ENDER_STONE("ENDER_STONE", "Ender Stone"),
    DIAMOND("DIAMOND", "Diamond"),
    ENCHANTED_DIAMOND("ENCHANTED_DIAMOND", "Enchanted Diamond", "E Diamond"),
    ENCHANTED_DIAMOND_BLOCK("ENCHANTED_DIAMOND_BLOCK", "Enchanted Diamond Block", "E Diamond Block"),
    IRON_INGOT("IRON_INGOT", "Iron Ingot"),
    ENCHANTED_IRON("ENCHANTED_IRON", "Enchanted Iron", "E Iron"),
    ENCHANTED_IRON_BLOCK("ENCHANTED_IRON_BLOCK", "Enchanted Iron Block", "E Iron Block"),
    GOLD_INGOT("GOLD_INGOT", "Gold Ingot"),
    ENCHANTED_GOLD("ENCHANTED_GOLD", "Enchanted Gold", "E Gold"),
    ENCHANTED_GOLD_BLOCK("ENCHANTED_GOLD_BLOCK", "Enchanted Gold Block", "E Gold Block"),
    REDSTONE("REDSTONE", "Redstone"),
    ENCHANTED_REDSTONE("ENCHANTED_REDSTONE", "Enchanted Redstone", "E Redstone"),
    ENCHANTED_REDSTONE_BLOCK("ENCHANTED_REDSTONE_BLOCK", "Enchanted Redstone Block", "E Redstone Block"),
    ENCHANTED_REDSTONE_LAMP("ENCHANTED_REDSTONE_LAMP", "Enchanted Redstone Lamp", "E Redstone Lamp", "E Lamp"),
    LAPIS_LAZULI("INK_SACK:4", "Lapis Lazuli", "Lapis"),
    ENCHANTED_LAPIS_LAZULI("ENCHANTED_LAPIS_LAZULI", "Enchanted Lapis Lazuli", "E Lapis Lazuli", "E Lapis"),
    ENCHANTED_LAPIS_LAZULI_BLOCK("ENCHANTED_LAPIS_LAZULI_BLOCK", "Enchanted Lapis Lazuli Block", "E Lapis Lazuli Block", "E Lapis Block"),
    EMERALD("EMERALD", "Emerald"),
    ENCHANTED_EMERALD("ENCHANTED_EMERALD", "Enchanted Emerald", "E Emerald"),
    ENCHANTED_EMERALD_BLOCK("ENCHANTED_EMERALD_BLOCK", "Enchanted Emerald Block", "E Emerald Block"),
    OBSIDIAN("OBSIDIAN", "Obsidian"),
    ENCHANTED_OBSIDIAN("ENCHANTED_OBSIDIAN", "Enchanted Obsidian", "E Obsidian"),
    COAL("COAL", "Coal"),
    ENCHANTED_COAL("ENCHANTED_COAL", "Enchanted Coal", "E Coal"),
    ENCHANTED_CHARCOAL("ENCHANTED_CHARCOAL", "Enchanted Charcoal", "E Charcoal"),
    ENCHANTED_COAL_BLOCK("ENCHANTED_COAL_BLOCK", "Enchanted Coal Block", "E Coal Block"),
    GRAVEL("GRAVEL", "Gravel"),
    FLINT("FLINT", "Flint"),
    GLOWSTONE_DUST("GLOWSTONE_DUST", "Glowstone Dust"),
    ENCHANTED_GLOWSTONE_DUST("ENCHANTED_GLOWSTONE_DUST", "Enchanted Glowstone Dust", "E Glowstone Dust", "E Glowstone"),
    ENCHANTED_GLOWSTONE("ENCHANTED_GLOWSTONE", "Enchanted Glowstone Block", "E Glowstone Block"),
    ENCHANTED_FLINT("ENCHANTED_FLINT", "Enchanted Flint", "E Flint"),
    ICE("ICE", "Ice"),
    ENCHANTED_ICE("ENCHANTED_ICE", "Enchanted Ice", "E Ice"),
    PACKED_ICE("PACKED_ICE", "Packed Ice"),
    ENCHANTED_PACKED_ICE("ENCHANTED_PACKED_ICE", "Enchanted Packed Ice", "E Packed Ice"),
    SNOW_BALL("SNOW_BALL", "Snow Ball"),
    SNOW_BLOCK("SNOW_BLOCK", "Snow Block"),
    ENCHANTED_SNOW_BLOCK("ENCHANTED_SNOW_BLOCK", "Enchanted Snow Block", "E Snow Block"),
    ENCHANTED_ENDSTONE("ENCHANTED_ENDSTONE", "Enchanted Endstone", "E Endstone"),
    SAND("SAND", "Sand"),
    ENCHANTED_SAND("ENCHANTED_SAND", "Enchanted Sand", "E Sand"),
    NETHERRACK("NETHERRACK", "Netherrack"),
    NETHER_STALK("NETHER_STALK", "Nether Stalk"),
    ENCHANTED_NETHER_STALK("ENCHANTED_NETHER_STALK", "Enchanted Nether Stalk", "E Nether Stalk"),
    QUARTZ("QUARTZ", "Quartz"),
    ENCHANTED_QUARTZ("ENCHANTED_QUARTZ", "Enchanted Quartz", "E Quartz"),
    ENCHANTED_QUARTZ_BLOCK("ENCHANTED_QUARTZ_BLOCK", "Enchanted Quartz Block", "E Quartz Block"),
    ENCHANTED_LAVA_BUCKET("ENCHANTED_LAVA_BUCKET", "Enchanted Lava Bucket", "E Lava Bucket"),

    // Combat
    STRING("STRING", "String"),
    ENCHANTED_STRING("ENCHANTED_STRING", "Enchanted String", "E String"),
    ENDER_PEARL("ENDER_PEARL", "Ender Pearl"),
    ENCHANTED_ENDER_PEARL("ENCHANTED_ENDER_PEARL", "Enchanted Ender Pearl", "E Ender Pearl"),
    ENCHANTED_EYE_OF_ENDER("ENCHANTED_EYE_OF_ENDER", "Enchanted Eye of Ender", "E Eye of Ender"),
    BONE("BONE", "Bone"),
    ENCHANTED_BONE("ENCHANTED_BONE", "Enchanted Bone", "E Bone"),
    ROTTEN_FLESH("ROTTEN_FLESH", "Rotten Flesh"),
    ENCHANTED_ROTTEN_FLESH("ENCHANTED_ROTTEN_FLESH", "Enchanted Rotten Flesh", "E Rotten Flesh"),
    SLIME_BALL("SLIME_BALL", "Slime Ball"),
    ENCHANTED_SLIME_BALL("ENCHANTED_SLIME_BALL", "Enchanted Slime Ball", "E Slime Ball"),
    ENCHANTED_SLIME_BLOCK("ENCHANTED_SLIME_BLOCK", "Enchanted Slime Block", "E Slime Block"),
    SULPHUR("SULPHUR", "Gunpowder"),
    ENCHANTED_GUNPOWDER("ENCHANTED_GUNPOWDER", "Enchanted Gunpowder", "E Gunpowder"),
    ENCHANTED_FIREWORK_ROCKET("ENCHANTED_FIREWORK_ROCKET", "Enchanted Firework Rocket", "E Firework Rocket", "E Firework"),
    BLAZE_ROD("BLAZE_ROD", "Blaze Rod", "Blaze"),
    ENCHANTED_BLAZE_POWDER("ENCHANTED_BLAZE_POWDER", "Enchanted Blaze Powder", "E Blaze Powder", "E Blaze"),
    ENCHANTED_BLAZE_ROD("ENCHANTED_BLAZE_ROD", "Enchanted Blaze Rod", "E Blaze Rod"),
    SPIDER_EYE("SPIDER_EYE", "Spider Eye"),
    ENCHANTED_SPIDER_EYE("ENCHANTED_SPIDER_EYE", "Enchanted Spider Eye", "E Spider Eye"),
    ENCHANTED_FERMENTED_SPIDER_EYE("ENCHANTED_FERMENTED_SPIDER_EYE", "Enchanted Fermented Spider Eye", "E Fermented Spider Eye"),
    GHAST_TEAR("GHAST_TEAR", "Ghast Tear"),
    ENCHANTED_GHAST_TEAR("ENCHANTED_GHAST_TEAR", "Enchanted Ghast Tear", "E Ghast Tear"),
    MAGMA_CREAM("MAGMA_CREAM", "Magma Cream"),
    ENCHANTED_MAGMA_CREAM("ENCHANTED_MAGMA_CREAM", "Enchanted Magma Cream", "E Magma Cream"),

    // Wood,
    OAK("LOG", "Oak Log", "Oak"),
    ENCHANTED_OAK_LOG("LOG", "Enchanted Oak Log", "E Oak Log", "E Oak"),
    BIRCH("LOG:2", "Birch Log", "Birch"),
    ENCHANTED_BIRCH_LOG("ENCHANTED_BIRCH_LOG", "Enchanted Birch Log", "E Birch Log", "E Birch"),
    ACACIA("LOG_2", "Acacia Log", "Acacia"),
    ENCHANTED_ACACIA_LOG("ENCHANTED_ACACIA_LOG", "Enchanted Acacia Log", "E Acacia Log", "E Acacia"),
    JUNGLE("LOG:3", "Jungle Log", "Jungle"),
    ENCHANTED_JUNGLE_LOG("ENCHANTED_JUNGLE_LOG", "Enchanted Jungle Log", "E Jungle Log", "E Jungle"),
    DARK_OAK("LOG_2:1", "Dark Oak Log", "Dark Oak"),
    ENCHANTED_DARK_OAK_LOG("ENCHANTED_DARK_OAK_LOG", "Enchanted Dark Oak Log", "E Dark Oak Log", "E Dark Oak"),
    SPRUCE("LOG:1", "Spruce Log", "Spruce"),
    ENCHANTED_SPRUCE_LOG("ENCHANTED_SPRUCE_LOG", "Enchanted Spruce Log", "E Spruce Log", "E Spruce"),

    // Fishing
    INK_SACK("INK_SACK", "Ink Sack", "Ink"),
    ENCHANTED_INK_SACK("ENCHANTED_INK_SACK", "Enchanted Ink Sack", "E Ink Sack", "E Ink"),
    RAW_FISH("RAW_FISH", "Raw Fish"),
    ENCHANTED_RAW_FISH("ENCHANTED_RAW_FISH", "Enchanted Raw Fish", "E Raw Fish"),
    ENCHANTED_COOKED_FISH("ENCHANTED_COOKED_FISH", "Enchanted Cooked Fish", "E Cooked Fish"),
    RAW_SALMON("RAW_FISH:1", "Raw Salmon"),
    ENCHANTED_COOKED_SALMON("ENCHANTED_COOKED_SALMON", "Enchanted Cooked Salmon", "E Cooked Salmon"),
    ENCHANTED_PUFFERFISH("ENCHANTED_PUFFERFISH", "Enchanted Pufferfish", "E Pufferfish"),
    ENCHANTED_RAW_SALMON("ENCHANTED_RAW_SALMON", "Enchanted Raw Salmon", "E Raw Salmon"),
    PRISMARINE_SHARD("PRISMARINE_SHARD", "Prismarine Shard", "Prismarine"),
    ENCHANTED_PRISMARINE_SHARD("ENCHANTED_PRISMARINE_SHARD", "Enchanted Prismarine Shard", "E Prismarine Shard"),
    PRISMARINE_CRYSTALS("PRISMARINE_CRYSTALS", "Prismarine Crystal"),
    ENCHANTED_PRISMARINE_CRYSTALS("ENCHANTED_PRISMARINE_CRYSTALS", "Enchanted Prismarine Crystal", "E Prismarine Crystal"),
    SPONGE("SPONGE", "Sponge"),
    ENCHANTED_SPONGE("ENCHANTED_SPONGE", "Enchanted Sponge", "E Sponge"),
    ENCHANTED_WET_SPONGE("ENCHANTED_WET_SPONGE", "Enchanted Wet Sponge", "E Wet Sponge"),
    PUFFERFISH("RAW_FISH:3", "Pufferfish"),
    CLONFISH("RAW_FISH:2", "Clownfish"),
    ENCHANTED_CLOWNFISH("ENCHANTED_CLOWNFISH", "Enchanted Clownfish", "E Clownfish"),
    WATER_LILY("WATER_LILY", "Lilypad"),
    ENCHANTED_WATER_LILY("ENCHANTED_WATER_LILY", "Enchanted Lilypad", "E Lilypad"),
    CLAY_BALL("CLAY_BALL", "Clay"),
    ENCHANTED_CLAY_BALL("ENCHANTED_CLAY_BALL", "Enchanted Clay", "E Clay"),

    // Misc
    STOCK_OF_STONKS("STOCK_OF_STONKS", "Stocks of Stonks", "Stocks", "Stonks"),
    GREEN_CANDY("GREEN_CANDY", "Green Candy"),
    PURPLE_CANDY("PURPLE_CANDY", "Purple Candy"),
    WHITE_GIFT("WHITE_GIFT", "White Gift"),
    GREEN_GIFT("GREEN_GIFT", "Green Gift"),
    RED_GIFT("RED_GIFT", "Red Gift"),
    CATALYST("CATALYST", "Catalyst"),
    HAMSTER_WHEEL("HAMSTER_WHEEL", "Hamster Wheel", "Hamster"),
    HOT_POTATO_BOOK("HOT_POTATO_BOOK", "Hot Potato Book", "Hot Potato"),
    TARANTULA_WEB("TARANTULA_WEB", "Tarantula Web"),
    TARANTULA_SILK("TARANTULA_SILK", "Tarantula Silk"),
    WOLF_TOOTH("WOLF_TOOTH", "Wolf Tooth"),
    GOLDEN_TOOTH("GOLDEN_TOOTH", "Golden Tooth"),
    FOUL_FLESH("FOUL_FLESH", "Foul Flesh"),
    REVENANT_FLESH("REVENANT_FLESH", "Revenant Flesh"),
    REVENANT_VISCERA("REVENANT_VISCERA", "Revenant Viscera"),
    COMPACTOR("COMPACTOR", "Compactor"),
    SUPER_COMPACTOR_3000("SUPER_COMPACTOR_3000", "Super Compactor 3000", "3000"),
    PROTECTOR_FRAGMENT("PROTECTOR_FRAGMENT", "Protector Fragment", "Prot Frag"),
    OLD_FRAGMENT("OLD_FRAGMENT", "Old Fragment", "Old Frag"),
    WISE_FRAGMENT("WISE_FRAGMENT", "Wise Fragment", "Wise Frag"),
    STRONG_FRAGMENT("STRONG_FRAGMENT", "Strong Fragment", "Strong Frag"),
    YOUNG_FRAGMENT("YOUNG_FRAGMENT", "Young Fragment", "Young Frag"),
    UNSTABLE_FRAGMENT("UNSTABLE_FRAGMENT", "Unstable Fragment", "Unstable Frag"),
    SUPERIOR_FRAGMENT("SUPERIOR_FRAGMENT", "Superior Fragment", "Superior Frag", "Sup Frag"),
    SUMMONING_EYE("SUMMONING_EYE", "Summoning Eye", "Eye"),
    GRIFFIN_FEATHER("GRIFFIN_FEATHER", "Griffin Feather", "Griffin"),
    DAEDALUS_STICK("DAEDALUS_STICK", "Daedalus Stick", "Daedalus"),
    ANCIENT_CLAW("ANCIENT_CLAW", "Ancient Claw"),
    ENCHANTED_ANCIENT_CLAW("ENCHANTED_ANCIENT_CLAW", "Enchanted Ancient Claw", "E Ancient Claw"),
    BOOSTER_COOKIE("BOOSTER_COOKIE", "Booster Cookie", "Boost");

    private final String key;
    private final String name;
    private final List<String> names;

    BazaarProduct(String key, String name, String... names) {
        this.key = key;
        this.name = name;
        this.names = new ArrayList<>(Arrays.asList(names));
        this.names.add(name);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static BazaarProduct getFromKey(String key) {
        for (BazaarProduct product : values()) {
            if (product.getKey().equalsIgnoreCase(key)) {
                return product;
            }
        }
        return null;
    }

    public static BazaarProduct getFromName(String name) {
        for (BazaarProduct product : values()) {
            for (String productName : product.names) {
                if (productName.equalsIgnoreCase(name)) {
                    return product;
                }
            }
        }
        return null;
    }
}
