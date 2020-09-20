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

package com.senither.hypixel.hypixel.response;

import com.senither.hypixel.contracts.hypixel.Response;
import com.senither.hypixel.time.Carbon;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class GuildLeaderboardResponse extends Response {

    protected List<Guild> data;

    public List<Guild> getData() {
        return data;
    }

    public class Guild {

        protected String id;
        protected String name;
        protected double average_skill;
        protected double average_skill_progress;
        protected double average_slayer;
        protected double average_catacomb;
        protected int members;
        protected GuildWeight weight;
        private String last_updated_at;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getMembers() {
            return members;
        }

        public double getAverageSkill() {
            return average_skill;
        }

        public double getAverageSkillProgress() {
            return average_skill_progress;
        }

        public double getAverageSlayer() {
            return average_slayer;
        }

        public double getAverageCatacomb() {
            return average_catacomb;
        }

        public GuildWeight getWeight() {
            return weight;
        }

        public Carbon getLastUpdatedAt() {
            return timestampToCarbonInstance(last_updated_at);
        }

        public class GuildWeight {

            protected double total;
            protected double skill;
            protected double slayer;
            protected double multiplier;

            public double getTotal() {
                return total;
            }

            public double getSkill() {
                return skill;
            }

            public double getSlayer() {
                return slayer;
            }

            public double getMultiplier() {
                return multiplier;
            }
        }
    }
}
