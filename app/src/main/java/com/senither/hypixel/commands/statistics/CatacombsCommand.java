package com.senither.hypixel.commands.statistics;

import com.google.gson.JsonObject;
import com.senither.hypixel.SkyblockAssistant;
import com.senither.hypixel.chat.MessageType;
import com.senither.hypixel.contracts.commands.SkillCommand;
import com.senither.hypixel.statistics.StatisticsChecker;
import com.senither.hypixel.statistics.responses.DungeonResponse;
import com.senither.hypixel.time.Carbon;
import com.senither.hypixel.utils.NumberUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CatacombsCommand extends SkillCommand {

    public CatacombsCommand(SkyblockAssistant app) {
        super(app, "dungeon catacomb");
    }

    @Override
    public String getName() {
        return "Catacombs Command";
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "Gets information about a players catacomb level,",
            "as-well-as their class levels, and best runs."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <username> [profile]` - Gets catacomb info for the given username",
            "`:command <mention> [profile]` - Gets catacomb info for the mentioned Discord user"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("catacomb", "cata", "ca", "dungeon", "dung");
    }

    @Override
    protected void handleSkyblockProfile(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, String[] args) {
        JsonObject member = getProfileMemberFromPlayer(profileReply, playerReply);
        DungeonResponse response = StatisticsChecker.DUNGEON.checkUser(playerReply, profileReply, member);

        if (!hasResponseData(message, profileReply, playerReply, response)) {
            return;
        }

        DungeonResponse.Dungeon catacomb = response.getDungeons().get(DungeonResponse.DungeonType.CATACOMBS);

        String description = String.format("**%s** is catacomb level **%s** with",
            getUsernameFromPlayer(playerReply),
            NumberUtil.formatNicelyWithDecimals(catacomb.getLevel())
        );

        description += response.getSelectedClass() == null
            ? " no class selected."
            : String.format(
            " the **%s** class selected.",
            uppercaseFirstCharacter(response.getSelectedClass().name().toLowerCase())
        );

        EmbedBuilder builder = new EmbedBuilder()
            .setColor(MessageType.SUCCESS.getColor())
            .setTitle(message.getEmbeds().get(0).getTitle())
            .setDescription(description)
            .setFooter(String.format(
                "Profile: %s",
                profileReply.getProfile().get("cute_name").getAsString()
            ))
            .setTimestamp(Carbon.now().setTimestamp(member.get("last_save").getAsLong() / 1000L).getTime().toInstant());

        for (Map.Entry<DungeonResponse.DungeonClassType, DungeonResponse.DungeonClass> dungeonClassEntry : response.getPlayerClasses().entrySet()) {
            builder.addField(
                uppercaseFirstCharacter(dungeonClassEntry.getKey().name().toLowerCase()),
                String.format("**LvL:** %s\n**EXP**: %s",
                    NumberUtil.formatNicelyWithDecimals(dungeonClassEntry.getValue().getLevel()),
                    NumberUtil.formatNicelyWithDecimals(dungeonClassEntry.getValue().getExperience())
                ), true
            );
        }

        List<String> floorClears = new ArrayList<>();
        for (Map.Entry<Integer, Integer> timesPlayedEntry : catacomb.getTimesPlayed().entrySet()) {
            String name = timesPlayedEntry.getKey() == 0 ? "Entrance" : "Floor " + timesPlayedEntry.getKey();
            floorClears.add(padSpaces(name + ": ", 10) + NumberUtil.formatNicely(timesPlayedEntry.getValue()));
        }

        builder.addField(
            String.format("Floor Clears (Highest cleared floor is %d)", catacomb.getHighestFloorCleared()),
            String.format("```yml\n%s```", String.join("\n", floorClears)),
            false
        );

        List<String> bestScores = new ArrayList<>();
        for (Map.Entry<Integer, DungeonResponse.DungeonScore> timesPlayedEntry : catacomb.getBestScores().entrySet()) {
            String name = timesPlayedEntry.getKey() == 0 ? "Entrance" : "Floor " + timesPlayedEntry.getKey();
            bestScores.add(padSpaces(name + ": ", 10) + String.format("%d (%s)",
                timesPlayedEntry.getValue().getValue(),
                timesPlayedEntry.getValue().getScore()
            ));
        }

        builder.addField(
            "Best Floor Scores",
            String.format("```yml\n%s```", String.join("\n", bestScores)),
            true
        );

        message.editMessage(builder.build()).queue();
    }

    private String uppercaseFirstCharacter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    private boolean hasResponseData(Message message, SkyBlockProfileReply profileReply, PlayerReply playerReply, DungeonResponse response) {
        if (!response.hasData()) {
            message.editMessage(new EmbedBuilder()
                .setColor(MessageType.WARNING.getColor())
                .setTitle(message.getEmbeds().get(0).getTitle())
                .setDescription(String.format(
                    "**%s** doesn't have any completed dungeon runs in their **%s** profile!\n",
                    getUsernameFromPlayer(playerReply), profileReply.getProfile().get("cute_name").getAsString()
                ))
                .build()
            ).queue();
            return false;
        }

        return true;
    }

    private String padSpaces(String string, double size) {
        StringBuilder builder = new StringBuilder(string);
        while (builder.length() < size) {
            builder.append(" ");
        }
        return builder.toString();
    }
}
