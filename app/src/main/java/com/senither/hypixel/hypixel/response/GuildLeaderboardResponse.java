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

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class GuildLeaderboardResponse {

    protected int status;
    protected List<Guild> data;

    public boolean isSuccess() {
        return getStatus() == 200;
    }

    public int getStatus() {
        return status;
    }

    public List<Guild> getData() {
        return data;
    }

    public class Guild {

        protected String id;
        protected String name;
        private int members;
        protected double average_skill;
        protected double average_slayer;

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

        public double getAverageSlayer() {
            return average_slayer;
        }
    }
}
