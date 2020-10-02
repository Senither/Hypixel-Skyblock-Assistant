package com.senither.hypixel.statistics.checker;

import com.google.gson.JsonObject;
import com.senither.hypixel.contracts.statistics.Checker;
import com.senither.hypixel.statistics.responses.DungeonResponse;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.skyblock.SkyBlockProfileReply;

import javax.annotation.Nullable;

public class DungeonChecker extends Checker<DungeonResponse> {

    @Override
    public DungeonResponse checkUser(@Nullable PlayerReply playerReply, SkyBlockProfileReply profileReply, JsonObject member) {
        JsonObject dungeons = member.getAsJsonObject("dungeons");
        if (!hasDungeonData(dungeons)) {
            return new DungeonResponse(true, false);
        }

        DungeonResponse response = new DungeonResponse(true, true);

        response.setSelectedClass(dungeons.has("selected_dungeon_class")
            ? DungeonResponse.DungeonClassType.fromName(dungeons.get("selected_dungeon_class").getAsString())
            : null
        );

        JsonObject playerClasses = dungeons.getAsJsonObject("player_classes");
        for (DungeonResponse.DungeonClassType dungeonClassType : DungeonResponse.DungeonClassType.values()) {
            JsonObject dungeonClass = playerClasses.getAsJsonObject(dungeonClassType.name().toLowerCase());

            response.setPlayerClassExperience(
                dungeonClassType,
                dungeonClass.has("experience")
                    ? dungeonClass.get("experience").getAsDouble()
                    : 0D
            );
        }

        JsonObject dungeonTypes = dungeons.getAsJsonObject("dungeon_types");
        for (DungeonResponse.DungeonType dungeonType : DungeonResponse.DungeonType.values()) {
            response.setDungeonContent(dungeonType, dungeonTypes.getAsJsonObject(dungeonType.name().toLowerCase()));
        }

        return response;
    }

    private boolean hasDungeonData(JsonObject object) {
        return object != null
            && object.has("dungeon_types")
            && object.has("player_classes")
            && object.getAsJsonObject("dungeon_types").has("catacombs")
            && object.getAsJsonObject("dungeon_types").getAsJsonObject("catacombs").has("experience")
            && object.getAsJsonObject("dungeon_types").getAsJsonObject("catacombs").has("times_played");
    }
}
