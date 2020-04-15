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

import java.util.List;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class PlayerLeaderboardResponse extends Response {

    protected List<Player> data;

    public List<Player> getData() {
        return data;
    }

    public class Player {

        protected UUID uuid;
        protected String username;
        protected double average_skill;
        protected double total_slayer;
        protected double mining;
        protected double foraging;
        protected double enchanting;
        protected double farming;
        protected double combat;
        protected double fishing;
        protected double alchemy;
        protected double carpentry;
        protected double runecrafting;

        public UUID getUuid() {
            return uuid;
        }

        public String getUsername() {
            return username;
        }

        public double getAverageSkill() {
            return average_skill;
        }

        public double getTotalSlayer() {
            return total_slayer;
        }

        public double getMining() {
            return mining;
        }

        public double getForaging() {
            return foraging;
        }

        public double getEnchanting() {
            return enchanting;
        }

        public double getFarming() {
            return farming;
        }

        public double getCombat() {
            return combat;
        }

        public double getFishing() {
            return fishing;
        }

        public double getAlchemy() {
            return alchemy;
        }

        public double getCarpentry() {
            return carpentry;
        }

        public double getRunecrafting() {
            return runecrafting;
        }
    }
}
