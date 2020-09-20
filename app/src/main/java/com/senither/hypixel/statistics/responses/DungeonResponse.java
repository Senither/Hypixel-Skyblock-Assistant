package com.senither.hypixel.statistics.responses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.HasLevel;
import com.senither.hypixel.contracts.statistics.Jsonable;
import com.senither.hypixel.contracts.statistics.StatisticsResponse;
import com.senither.hypixel.utils.NumberUtil;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DungeonResponse extends StatisticsResponse implements Jsonable {

    private final boolean hasData;

    private final EnumMap<DungeonType, Dungeon> dungeons = new EnumMap<>(DungeonType.class);
    private final EnumMap<DungeonClassType, DungeonClass> playerClasses = new EnumMap<>(DungeonClassType.class);

    private DungeonClassType selectedClass = null;

    public DungeonResponse(boolean apiEnable, boolean hasData) {
        super(apiEnable);

        this.hasData = hasData;
    }

    public boolean hasData() {
        return hasData;
    }

    public DungeonClassType getSelectedClass() {
        return selectedClass;
    }

    public void setSelectedClass(DungeonClassType selectedClass) {
        this.selectedClass = selectedClass;
    }

    public EnumMap<DungeonType, Dungeon> getDungeons() {
        return dungeons;
    }

    public Dungeon getDungeonFromType(DungeonType type) {
        return dungeons.getOrDefault(type, null);
    }

    public void setDungeonContent(DungeonType dungeonType, JsonObject object) {
        dungeons.put(dungeonType, new Dungeon(object));
    }

    public EnumMap<DungeonClassType, DungeonClass> getPlayerClasses() {
        return playerClasses;
    }

    public DungeonClass getClassFromType(DungeonClassType type) {
        return playerClasses.getOrDefault(type, null);
    }

    public void setPlayerClassExperience(DungeonClassType dungeonClassType, double experience) {
        playerClasses.put(dungeonClassType, new DungeonClass(experience));
    }

    private double getLevelFromExperience(double experience) {
        int level = 0;
        for (int toRemove : Constants.DUNGEON_EXPERIENCE) {
            experience -= toRemove;
            if (experience < 0) {
                return level + (1D - (experience * -1) / (double) toRemove);
            }
            level++;
        }
        return level;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("selected_class", selectedClass == null ? "none" : selectedClass.name().toLowerCase());

        JsonObject dungeonClasses = new JsonObject();
        for (Map.Entry<DungeonClassType, DungeonClass> dungeonClassEntry : playerClasses.entrySet()) {
            dungeonClasses.add(dungeonClassEntry.getKey().name().toLowerCase(), dungeonClassEntry.getValue().toJson());
        }
        json.add("classes", dungeonClasses);

        JsonObject dungeonTypes = new JsonObject();
        for (Map.Entry<DungeonType, Dungeon> dungeonClassEntry : dungeons.entrySet()) {
            dungeonTypes.add(dungeonClassEntry.getKey().name().toLowerCase(), dungeonClassEntry.getValue().toJson());
        }
        json.add("types", dungeonTypes);

        return json;
    }

    public enum DungeonType {

        CATACOMBS
    }

    public enum DungeonClassType {

        HEALER, MAGE, BERSERK, ARCHER, TANK;

        public static DungeonClassType fromName(String name) {
            for (DungeonClassType value : DungeonClassType.values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }
    }

    public class Dungeon implements Jsonable, HasLevel {

        private final double experience;
        private final double level;
        private final int highestFloorCleared;
        private final LinkedHashMap<Integer, Integer> timesPlayed = new LinkedHashMap<>();
        private final LinkedHashMap<Integer, DungeonScore> bestScores = new LinkedHashMap<>();

        public Dungeon(JsonObject object) {
            this.experience = object.get("experience").getAsDouble();
            this.level = getLevelFromExperience(experience);

            for (Map.Entry<String, JsonElement> played : object.getAsJsonObject("times_played").entrySet()) {
                timesPlayed.put(NumberUtil.parseInt(played.getKey(), 0), played.getValue().getAsInt());
            }
            this.highestFloorCleared = timesPlayed.size() - 1;

            for (Map.Entry<String, JsonElement> played : object.getAsJsonObject("best_score").entrySet()) {
                bestScores.put(NumberUtil.parseInt(played.getKey(), 0), new DungeonScore(played.getValue().getAsInt()));
            }
        }

        @Override
        public double getExperience() {
            return experience;
        }

        @Override
        public double getLevel() {
            return level;
        }

        public int getHighestFloorCleared() {
            return highestFloorCleared;
        }

        public LinkedHashMap<Integer, Integer> getTimesPlayed() {
            return timesPlayed;
        }

        public LinkedHashMap<Integer, DungeonScore> getBestScores() {
            return bestScores;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();

            json.addProperty("level", level);
            json.addProperty("experience", experience);

            JsonObject played = new JsonObject();
            for (Map.Entry<Integer, Integer> timesPlayedEntry : timesPlayed.entrySet()) {
                played.addProperty(
                    timesPlayedEntry.getKey() == 0 ? "entrance" : timesPlayedEntry.getKey().toString(),
                    timesPlayedEntry.getValue()
                );
            }
            json.add("times_played", played);

            JsonObject scores = new JsonObject();
            for (Map.Entry<Integer, DungeonScore> bestScoresEntry : bestScores.entrySet()) {
                scores.add(
                    bestScoresEntry.getKey() == 0 ? "entrance" : bestScoresEntry.getKey().toString(),
                    bestScoresEntry.getValue().toJson()
                );
            }
            json.add("best_scores", scores);

            return json;
        }
    }

    public class DungeonScore implements Jsonable {

        private final int value;
        private final String score;

        public DungeonScore(int value) {
            this.value = value;

            if (value >= 300) {
                score = "S+";
            } else if (value >= 270) {
                score = "S";
            } else if (value >= 240) {
                score = "A";
            } else if (value >= 175) {
                score = "B";
            } else {
                score = "C";
            }
        }

        public int getValue() {
            return value;
        }

        public String getScore() {
            return score;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();

            json.addProperty("value", value);
            json.addProperty("score", score);

            return json;
        }
    }

    public class DungeonClass implements Jsonable, HasLevel {

        private final double experience;
        private final double level;

        public DungeonClass(double experience) {
            this.experience = experience;
            this.level = getLevelFromExperience(experience);
        }

        @Override
        public double getExperience() {
            return experience;
        }

        @Override
        public double getLevel() {
            return level;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();

            json.addProperty("level", level);
            json.addProperty("experience", experience);

            return json;
        }
    }
}
