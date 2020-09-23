package com.senither.hypixel.commands.calculators;

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageFactory;
import com.senither.hypixel.chat.PlaceholderMessage;
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.contracts.statistics.CanCalculateWeight;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.DungeonResponse;
import com.senither.hypixel.statistics.responses.SkillsResponse;
import com.senither.hypixel.statistics.responses.SlayerResponse;
import com.senither.hypixel.statistics.weight.DungeonWeight;
import com.senither.hypixel.statistics.weight.SkillWeight;
import com.senither.hypixel.statistics.weight.SlayerWeight;
import com.senither.hypixel.statistics.weight.Weight;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WeightCalculatorCommand extends SkillCommand {

    public WeightCalculatorCommand(SkyblockAssistant app) {
        super(app, "Weight");
    }

    @Override
    public String getName() {
        return "Weight Calculator Command";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "This command can be used to calculate the weight of any given SkyBlock profile,",
            "creating a number to represent the progress that have been made on the",
            "profile thus far.\n\n",
            "> **Note:** This command is still a work in progress, expect wight values to change,",
            "and more weight calculations to be added in the future."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username> [profile]` - Gets the weight for the given username",
            "`:command <mention> [profile]` - Gets the weight for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("weight", "we");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, String[] args) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);

        PlaceholderMessage placeholderMessage = MessageFactory.makeInfo(message,
            "**:name's** weight for their **:profile** profile is **:weight**.\n\n"
                + "> **Note:** This command is still a work in progress, expect wight values to change, and more weight calculations to be added in the future."
        ).setTitle(message.getEmbeds().get(0).getTitle());

        Weight skillWeight = applySkillWeight(placeholderMessage, profileReply, playerReply, member);
        Weight slayerWeight = applySlayerWeight(placeholderMessage, profileReply, playerReply, member);
        Weight dungeonWeight = applyDungeonWeight(placeholderMessage, profileReply, playerReply, member);

        message.editMessage(placeholderMessage
            .set("name", getUsernameFromPlayer(playerReply))
            .set("profile", profileReply.getProfile().get("cute_name").getAsString())
            .set("weight", skillWeight.add(slayerWeight).add(dungeonWeight))
            .setFooter(String.format(
                "Profile: %s",
                profileReply.getProfile().get("cute_name").getAsString()
            ))
            .buildEmbed()
        ).queueAfter(250, TimeUnit.MILLISECONDS);
    }

    private Weight applySkillWeight(PlaceholderMessage message, SkyBlockProfileReply profileReply, PlayerReply playerReply, JsonObject member) {
        SkillsResponse skillsResponse = StatisticsChecker.SKILLS.checkUser(playerReply, profileReply, member);
        if (!skillsResponse.isApiEnable()) {
            message.addField(
                "Skills Weight (API is disabled)",
                "The skills weight calculation has been skipped because the skills API is disabled",
                false
            );

            return new Weight();
        }

        Weight totalWeight = skillsResponse.calculateTotalWeight();

        List<String> skillWeights = new ArrayList<>();
        for (SkillWeight value : SkillWeight.values()) {
            SkillsResponse.SkillStat skillStatsRelation = value.getSkillStatsRelation(skillsResponse);

            skillWeights.add(String.format("%s > Lvl: %s Weight: %s",
                padSpaces(prettifyEnumName(value.name()), 10),
                padSpaces(NumberUtil.formatNicelyWithDecimals(skillStatsRelation.getLevel()), 6),
                skillStatsRelation.calculateWeight()
            ));
        }

        message.addField(
            "Skills Weight: " + totalWeight.toString(),
            String.format("```scala\n%s```", String.join("\n", skillWeights)),
            false
        );

        return totalWeight;
    }

    private Weight applySlayerWeight(PlaceholderMessage message, SkyBlockProfileReply profileReply, PlayerReply playerReply, JsonObject member) {
        SlayerResponse slayerResponse = StatisticsChecker.SLAYER.checkUser(playerReply, profileReply, member);
        if (!slayerResponse.isApiEnable()) {
            message.addField(
                "Slayer Weight (No data)",
                "There are no slayer data to calculate the weight for.",
                false
            );

            return new Weight();
        }

        double totalWeight = slayerResponse.calculateTotalWeight();
        List<String> slayerWeights = new ArrayList<>();
        for (SlayerWeight value : SlayerWeight.values()) {
            SlayerResponse.SlayerStat slayerStatsRelation = value.getSlayerStatsRelation(slayerResponse);

            slayerWeights.add(String.format("%s > EXP: %s Weight: %s",
                padSpaces(prettifyEnumName(value.name()), 10),
                padSpaces(NumberUtil.formatNicelyWithDecimals(slayerStatsRelation.getExperience()), 10),
                NumberUtil.formatNicelyWithDecimals(slayerStatsRelation.calculateWeight())
            ));
        }

        message.addField(
            "Slayer Weight: " + NumberUtil.formatNicelyWithDecimals(totalWeight),
            String.format("```scala\n%s```", String.join("\n", slayerWeights)),
            false
        );

        return new Weight(totalWeight, 0D);
    }

    private Weight applyDungeonWeight(PlaceholderMessage message, SkyBlockProfileReply profileReply, PlayerReply playerReply, JsonObject member) {
        DungeonResponse dungeonResponse = StatisticsChecker.DUNGEON.checkUser(playerReply, profileReply, member);
        if (!dungeonResponse.hasData()) {
            message.addField(
                "Dungeon Weight (No data)",
                "There are no dungeon data to calculate the weight for.",
                false
            );

            return new Weight();
        }

        Weight totalWeight = dungeonResponse.calculateTotalWeight();

        List<String> skillWeights = new ArrayList<>();
        for (DungeonWeight value : DungeonWeight.values()) {
            CanCalculateWeight skillStatsRelation = value.getCalculatorFromDungeon(dungeonResponse);

            skillWeights.add(String.format("%s > Lvl: %s Weight: %s",
                padSpaces(prettifyEnumName(value.name()), 10),
                padSpaces(NumberUtil.formatNicelyWithDecimals(skillStatsRelation.getLevel()), 6),
                skillStatsRelation.calculateWeight()
            ));
        }

        message.addField(
            "Dungeon Weight: " + totalWeight.toString(),
            String.format("```scala\n%s```", String.join("\n", skillWeights)),
            false
        );

        return totalWeight;
    }

    private String prettifyEnumName(String name) {
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private String padSpaces(String string, double size) {
        StringBuilder builder = new StringBuilder(string);
        while (builder.length() < size) {
            builder.append(" ");
        }
        return builder.toString();
    }
}
