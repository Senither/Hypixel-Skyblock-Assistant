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

package com.senither.hypixel.hypixel.leaderboard;

import com.senither.hypixel.contracts.hypixel.PlayerStatConversionFunction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum LeaderboardType {

    OVERVIEW(
        "Overview", Arrays.asList("guild", "overview", "view", "metrics", "metric", "ov"),
        null, null
    ),
    AVERAGE_SKILL(
        "Average Skill", Arrays.asList("skills", "skill", "sk"),
        LeaderboardPlayer::getAverageSkillProgress, LeaderboardPlayer::getAverageSkill,
        LeaderboardPlayer::getAverageSkillProgress, LeaderboardPlayer::getTotalSkillExperience
    ),
    WEIGHT("Weight", Arrays.asList("weight", "we"),
        player -> player.getWeight().getWeight(),
        player -> player.getWeight().getOverflow(),
        player -> player.getWeight().getTotal()
    ),
    TOTAL_SLAYER(
        "Total Slayer", Arrays.asList("slayers", "slayer", "sl"),
        LeaderboardPlayer::getTotalSlayer, null
    ),
    REVENANT(
        "Revenant Slayer", Arrays.asList("revenant", "rev", "zombie"),
        LeaderboardPlayer::getRevenantXP, null
    ),
    TARANTULA(
        "Tarantula Slayer", Arrays.asList("tarantula", "tara", "spider"),
        LeaderboardPlayer::getTarantulaXP, null
    ),
    SVEN(
        "Sven Slayer", Arrays.asList("sven", "wolf", "dog"),
        LeaderboardPlayer::getSvenXP, null
    ),
    SECRETS("Dungeon Secrets Found", Arrays.asList("secrets", "secret", "treasures", "treasure", "sec", "se"),
        LeaderboardPlayer::getSecretsFound, null
    ),
    CATACOMB("Catacombs", Arrays.asList("catacomb", "cata", "ca"),
        LeaderboardPlayer::getCatacomb, LeaderboardPlayer::getCatacombXp, LeaderboardPlayer::getCatacombXp
    ),
    HEALER("Healer", Arrays.asList("healer", "heal"),
        LeaderboardPlayer::getHealer, LeaderboardPlayer::getHealerXp, LeaderboardPlayer::getHealerXp
    ),
    MAGE("Mage", Arrays.asList("mage", "wizard"),
        LeaderboardPlayer::getMage, LeaderboardPlayer::getMageXp, LeaderboardPlayer::getMageXp
    ),
    BERSERK("Berserk", Arrays.asList("berserk", "berserker", "warrior"),
        LeaderboardPlayer::getBerserk, LeaderboardPlayer::getBerserkXp, LeaderboardPlayer::getBerserkXp
    ),
    ARCHER("Archer", Arrays.asList("archer", "bow"),
        LeaderboardPlayer::getArcher, LeaderboardPlayer::getArcherXp, LeaderboardPlayer::getArcherXp
    ),
    TANK("Tank", Collections.singletonList("tank"),
        LeaderboardPlayer::getTank, LeaderboardPlayer::getTankXp, LeaderboardPlayer::getTankXp
    ),
    MINING(
        "Mining", Arrays.asList("mining", "mine", "ore"),
        LeaderboardPlayer::getMining, LeaderboardPlayer::getMiningXP, LeaderboardPlayer::getMiningXP
    ),
    FORAGING(
        "Foraging", Arrays.asList("foraging", "forage", "tree"),
        LeaderboardPlayer::getForaging, LeaderboardPlayer::getForagingXP, LeaderboardPlayer::getForagingXP
    ),
    ENCHANTING(
        "Enchanting", Arrays.asList("enchanting", "enchant"),
        LeaderboardPlayer::getEnchanting, LeaderboardPlayer::getEnchantingXP, LeaderboardPlayer::getEnchantingXP
    ),
    FARMING(
        "Farming", Arrays.asList("farming", "farm"),
        LeaderboardPlayer::getFarming, LeaderboardPlayer::getFarmingXP, LeaderboardPlayer::getFarmingXP
    ),
    COMBAT(
        "Combat", Arrays.asList("combat", "fight"),
        LeaderboardPlayer::getCombat, LeaderboardPlayer::getCombatXP, LeaderboardPlayer::getCombatXP
    ),
    FISHING(
        "Fishing", Arrays.asList("fishing", "fish"),
        LeaderboardPlayer::getFishing, LeaderboardPlayer::getFishingXP, LeaderboardPlayer::getFishingXP
    ),
    ALCHEMY(
        "Alchemy", Arrays.asList("alchemy", "pot"),
        LeaderboardPlayer::getAlchemy, LeaderboardPlayer::getAlchemyXP, LeaderboardPlayer::getAlchemyXP
    ),
    TAMING(
        "Taming", Arrays.asList("taming", "tame", "pet"),
        LeaderboardPlayer::getTaming, LeaderboardPlayer::getTamingXP, LeaderboardPlayer::getTamingXP
    ),
    CARPENTRY(
        "Carpentry", Arrays.asList("carpentry", "craft"),
        LeaderboardPlayer::getCarpentry, LeaderboardPlayer::getCarpentryXP, LeaderboardPlayer::getCarpentryXP
    ),
    RUNECRAFTING(
        "Runecrafting", Arrays.asList("runecrafting", "rune"),
        LeaderboardPlayer::getRunecrafting, LeaderboardPlayer::getRunecraftingXP, LeaderboardPlayer::getRunecraftingXP
    );

    protected final String name;
    protected final List<String> aliases;
    protected final PlayerStatConversionFunction statFunction;
    protected final PlayerStatConversionFunction expFunction;
    protected final PlayerStatConversionFunction orderFunction;
    protected final PlayerStatConversionFunction indexFunction;

    LeaderboardType(
        String name,
        List<String> aliases,
        PlayerStatConversionFunction statFunction,
        PlayerStatConversionFunction expFunction,
        PlayerStatConversionFunction orderFunction,
        PlayerStatConversionFunction indexFunction
    ) {
        this.name = name;
        this.aliases = aliases;
        this.statFunction = statFunction;
        this.expFunction = expFunction;
        this.orderFunction = orderFunction;
        this.indexFunction = indexFunction;
    }

    LeaderboardType(
        String name,
        List<String> aliases,
        PlayerStatConversionFunction statFunction,
        PlayerStatConversionFunction expFunction,
        PlayerStatConversionFunction orderFunction
    ) {
        this(name, aliases, statFunction, expFunction, orderFunction, null);
    }

    LeaderboardType(
        String name,
        List<String> aliases,
        PlayerStatConversionFunction statFunction,
        PlayerStatConversionFunction expFunction
    ) {
        this(name, aliases, statFunction, expFunction, statFunction);
    }

    public static LeaderboardType fromName(String name) {
        if (name == null) {
            return null;
        }

        for (LeaderboardType leaderboardType : values()) {
            if (leaderboardType.aliases.contains(name.toLowerCase())) {
                return leaderboardType;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public PlayerStatConversionFunction getStatFunction() {
        return statFunction;
    }

    public PlayerStatConversionFunction getExpFunction() {
        return expFunction;
    }

    public PlayerStatConversionFunction getOrderFunction() {
        return orderFunction;
    }

    public PlayerStatConversionFunction getIndexFunction() {
        return indexFunction;
    }
}
