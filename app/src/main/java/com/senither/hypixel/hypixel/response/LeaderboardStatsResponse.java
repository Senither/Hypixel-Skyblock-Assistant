package com.senither.hypixel.hypixel.response;

import com.senither.hypixel.contracts.hypixel.Response;

public class LeaderboardStatsResponse extends Response {

    protected Stats data;

    public Stats getData() {
        return data;
    }

    public class Stats {

        protected int guilds;
        protected int players;

        public int getGuilds() {
            return guilds;
        }

        public int getPlayers() {
            return players;
        }
    }
}
