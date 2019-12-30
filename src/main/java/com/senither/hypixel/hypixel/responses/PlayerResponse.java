/*
 * Copyright (c) 2019.
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

package com.senither.hypixel.hypixel.responses;

import com.senither.hypixel.contracts.hypixel.Response;
import com.senither.hypixel.hypixel.HypixelAPI;

import java.util.HashMap;

@SuppressWarnings("WeakerAccess")
public class PlayerResponse extends Response {

    protected Player player;

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return HypixelAPI.getGson().toJson(this);
    }

    public class Player {

        protected String displayname;
        protected SocialMedia socialMedia;
        protected Stats stats;

        public String getDisplayname() {
            return displayname;
        }

        public SocialMedia getSocialMedia() {
            return socialMedia;
        }

        public Stats getStats() {
            return stats;
        }
    }

    public class SocialMedia {

        protected HashMap<String, String> links;

        public HashMap<String, String> getLinks() {
            return links;
        }
    }

    public class Stats {

        protected SkyBlockStats SkyBlock;

        public SkyBlockStats getSkyBlock() {
            return SkyBlock;
        }
    }

    public class SkyBlockStats {

        protected HashMap<String, SkyblockProfile> profiles;

        public HashMap<String, SkyblockProfile> getProfiles() {
            return profiles;
        }
    }

    private class SkyblockProfile {

        protected String profile_id;
        protected String cute_name;

        public String getProfileId() {
            return profile_id;
        }

        public String getName() {
            return cute_name;
        }
    }
}
