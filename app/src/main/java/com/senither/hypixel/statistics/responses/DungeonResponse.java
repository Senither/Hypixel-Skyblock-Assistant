package com.senither.hypixel.statistics.responses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.senither.hypixel.Constants;
import com.senither.hypixel.contracts.statistics.CanCalculateWeight;
import com.senither.hypixel.contracts.statistics.HasLevel;
import com.senither.hypixel.contracts.statistics.Jsonable;
import com.senither.hypixel.contracts.statistics.StatisticsResponse;
import com.senither.hypixel.statistics.weight.DungeonWeight;
import com.senither.hypixel.statistics.weight.Weight;
import com.senither.hypixel.time.Carbon;
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
        dungeons.put(dungeonType, new Dungeon(dungeonType.getWeight(), object));
    }

    public EnumMap<DungeonClassType, DungeonClass> getPlayerClasses() {
        return playerClasses;
    }

    public DungeonClass getClassFromType(DungeonClassType type) {
        return playerClasses.getOrDefault(type, null);
    }

    public void setPlayerClassExperience(DungeonClassType dungeonClassType, double experience) {
        playerClasses.put(dungeonClassType, new DungeonClass(dungeonClassType.getWeight(), experience));
    }

    public Weight calculateTotalWeight() {
        double weight = 0D;
        double overflow = 0D;

        for (DungeonWeight value : DungeonWeight.values()) {
            Weight dungeonWeight = value.getCalculatorFromDungeon(this).calculateWeight();

            weight += dungeonWeight.getWeight();
            overflow += dungeonWeight.getOverflow();
        }

        return new Weight(weight, overflow);
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

        CATACOMBS(DungeonWeight.CATACOMB);

        private final DungeonWeight weight;

        DungeonType(DungeonWeight weight) {
            this.weight = weight;
        }

        public DungeonWeight getWeight() {
            return weight;
        }
    }

    public enum DungeonClassType {

        HEALER(DungeonWeight.HEALER),
        MAGE(DungeonWeight.MAGE),
        BERSERK(DungeonWeight.BERSERK),
        ARCHER(DungeonWeight.ARCHER),
        TANK(DungeonWeight.TANK);

        private final DungeonWeight weight;

        DungeonClassType(DungeonWeight weight) {
            this.weight = weight;
        }

        public DungeonWeight getWeight() {
            return weight;
        }

        public static DungeonClassType fromName(String name) {
            for (DungeonClassType value : DungeonClassType.values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }
    }

    public class Dungeon implements Jsonable, HasLevel, CanCalculateWeight {

        private final DungeonWeight weight;
        private final int highestFloorCleared;
        private final LinkedHashMap<Integer, Integer> timesPlayed = new LinkedHashMap<>();
        private final LinkedHashMap<Integer, DungeonScore> bestScores = new LinkedHashMap<>();
        private final LinkedHashMap<Integer, DungeonTime> fastestTime = new LinkedHashMap<>();

        private double experience;
        private double level;

        public Dungeon(DungeonWeight weight, JsonObject object) {
            this.weight = weight;

            this.experience = object.get("experience").getAsDouble();
            this.level = getLevelFromExperience(experience);

            for (Map.Entry<String, JsonElement> played : object.getAsJsonObject("times_played").entrySet()) {
                timesPlayed.put(NumberUtil.parseInt(played.getKey(), 0), played.getValue().getAsInt());
            }
            this.highestFloorCleared = timesPlayed.size() - 1;

            if (object.has("best_score")) {
                for (Map.Entry<String, JsonElement> played : object.getAsJsonObject("best_score").entrySet()) {
                    bestScores.put(NumberUtil.parseInt(played.getKey(), 0), new DungeonScore(played.getValue().getAsInt()));
                }
            }

            if (object.has("fastest_time")) {
                for (Map.Entry<String, JsonElement> time : object.getAsJsonObject("fastest_time").entrySet()) {
                    fastestTime.put(NumberUtil.parseInt(time.getKey(), 0), new DungeonTime(time.getValue().getAsInt()));
                }
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

        public void setLevelAndExperience(double level, double experience) {
            this.level = level;
            this.experience = experience;
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

        public LinkedHashMap<Integer, DungeonTime> getFastestTime() {
            return fastestTime;
        }

        @Override
        public Weight calculateWeight() {
            return weight.calculateWeight(experience);
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

            JsonObject times = new JsonObject();
            for (Map.Entry<Integer, DungeonTime> dungeonTimeEntry : fastestTime.entrySet()) {
                times.add(
                    dungeonTimeEntry.getKey() == 0 ? "entrance" : dungeonTimeEntry.getKey().toString(),
                    dungeonTimeEntry.getValue().toJson()
                );
            }
            json.add("fastest_time", times);

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

    public class DungeonTime implements Jsonable {

        private final String time;
        private final int seconds;

        public DungeonTime(int timeInMilliseconds) {
            seconds = timeInMilliseconds / 1000;
            time = Carbon.now()
                .addSeconds(seconds)
                .diffForHumans(true);
        }

        public int getSeconds() {
            return seconds;
        }

        public String getTime() {
            return time;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();

            json.addProperty("time", time);
            json.addProperty("seconds", seconds);

            return json;
        }
    }

    public class DungeonClass implements Jsonable, HasLevel, CanCalculateWeight {

        private final DungeonWeight weight;
        private double experience;
        private double level;

        public DungeonClass(DungeonWeight weight, double experience) {
            this.weight = weight;

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

        public void setLevelAndExperience(double level, double experience) {
            this.level = level;
            this.experience = experience;
        }

        @Override
        public Weight calculateWeight() {
            return weight.calculateWeight(experience);
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
