<template>
    <article class="message">
        <div class="message-header" v-on:click="toggleCollaps(player)">
            <div class="columns is-gapless" style="width: 100%">
                <div class="column has-text-left">
                    <i
                        v-if="currentRank.name == 'Guild Master' || satisfiedRank.priority == currentRank.priority"
                        class="rank-status fas fa-check has-text-info"
                    ></i>
                    <i
                        v-else-if="satisfiedRank.priority < 0"
                        class="rank-status fas fa-times has-text-danger"
                    ></i>
                    <i
                        v-else-if="satisfiedRank.priority > currentRank.priority"
                        class="rank-status fas fa-arrow-up has-text-success"
                    ></i>
                    <i
                        v-else-if="satisfiedRank.priority < currentRank.priority"
                        class="rank-status fas fa-arrow-down has-text-warning"
                    ></i>
                    [<span class="rank">{{ currentRank.name }}</span>]
                    {{ player.username }}
                </div>
                <div class="column has-text-right uuid">{{ player.uuid }}</div>
            </div>
        </div>
        <div class="message-body" v-if="!player.collaps">
            <div class="columns">
                <div class="column" v-if="satisfiedRank.priority == -1">
                    <strong>{{ player.username }}</strong> doesn't meets the requirements for any rank in the guild!
                </div>
                <div class="column" v-else>
                    <strong>{{ player.username }}</strong> meets the rank requirements for <strong>{{ satisfiedRank.name }}</strong>!
                </div>
            </div>
            <div class="columns">
                <div class="column">
                    <h4 class="subtitle is-4">
                        Average Skills
                        <span
                            v-if="getRankFromPlayerReportEntity(this.player.checks.AVERAGE_SKILLS) != null"
                            class="individual-entry-rank"
                        >
                            {{ getRankFromPlayerReportEntity(this.player.checks.AVERAGE_SKILLS) }}
                        </span>
                    </h4>
                    <p v-if="getPlayerReportEntity(this.player.checks.AVERAGE_SKILLS) != null">
                        {{ formatNumber(getPlayerReportEntity(this.player.checks.AVERAGE_SKILLS).metric.amount.toFixed(2)) }} with level progress, and {{ formatNumber(getPlayerReportEntity(this.player.checks.AVERAGE_SKILLS).metric.amountWithoutProgress.toFixed(2)) }} without level progress.
                    </p>
                    <article v-else class="message is-danger">
                        <div class="message-header">
                            Skills API is disabled!
                        </div>
                    </article>
                </div>
                <div class="column">
                    <h4 class="subtitle is-4">
                        Bank & Purse
                        <span
                            v-if="getRankFromPlayerReportEntity(this.player.checks.BANK) != null"
                            class="individual-entry-rank"
                        >
                            {{ getRankFromPlayerReportEntity(this.player.checks.BANK) }}
                        </span>
                    </h4>
                    <p v-if="getPlayerReportEntity(this.player.checks.BANK) != null && getPlayerReportEntity(this.player.checks.BANK).metric.bank > 1">
                        {{ formatNumber(getPlayerReportEntity(this.player.checks.BANK).metric.amount) }} total coins,
                        with {{ formatNumber(getPlayerReportEntity(this.player.checks.BANK).metric.bank) }} in the bank,
                        and {{ formatNumber(getPlayerReportEntity(this.player.checks.BANK).metric.purse) }} in their purse.
                    </p>
                    <article v-else class="message is-danger">
                        <div class="message-header">
                            Bank API is disabled!
                        </div>
                    </article>
                </div>
            </div>

            <div class="columns">
                <div class="column">
                    <h4 class="subtitle is-4">
                        Slayer XP
                        <span
                            v-if="getRankFromPlayerReportEntity(this.player.checks.SLAYER) != null"
                            class="individual-entry-rank"
                        >
                            {{ getRankFromPlayerReportEntity(this.player.checks.SLAYER) }}
                        </span>
                    </h4>
                    <p v-if="getPlayerReportEntity(this.player.checks.SLAYER) != null">
                        {{ formatNumber(getPlayerReportEntity(this.player.checks.SLAYER).metric.amount) }} total Slayer XP
                    </p>
                    <article v-else class="message is-danger">
                        <div class="message-header">
                            The player does not have any slayer XP!
                        </div>
                    </article>
                </div>
                <div class="column">
                    <h4 class="subtitle is-4">
                        Fairy Souls
                        <span
                            v-if="getRankFromPlayerReportEntity(this.player.checks.FAIRY_SOULS) != null"
                            class="individual-entry-rank"
                        >
                            {{ getRankFromPlayerReportEntity(this.player.checks.FAIRY_SOULS) }}
                        </span>
                    </h4>
                    <p v-if="getPlayerReportEntity(this.player.checks.FAIRY_SOULS) != null">
                        {{ getPlayerReportEntity(this.player.checks.FAIRY_SOULS).metric.amount }} Fairy Souls
                    </p>
                    <article v-else class="message is-danger">
                        <div class="message-header">
                            The player have not collected any fairy souls yet!
                        </div>
                    </article>
                </div>
            </div>

            <div class="columns">
                <div class="column">
                    <h4 class="subtitle is-4">
                        Talismans
                        <span
                            v-if="getRankFromPlayerReportEntity(this.player.checks.TALISMANS) != null"
                            class="individual-entry-rank"
                        >
                            {{ getRankFromPlayerReportEntity(this.player.checks.TALISMANS) }}
                        </span>
                    </h4>
                    <p v-if="getPlayerReportEntity(this.player.checks.TALISMANS) != null">
                        {{ getPlayerReportEntity(this.player.checks.TALISMANS).metric.legendaries }} Legendaries
                        & {{ getPlayerReportEntity(this.player.checks.TALISMANS).metric.epics }} Epic talismans.
                    </p>
                    <article v-else class="message is-danger">
                        <div class="message-header">
                            Inventory API is disabled!
                        </div>
                    </article>
                </div>
                <div class="column">
                    <h4 class="subtitle is-4">
                        Power Orb
                        <span
                            v-if="getRankFromPlayerReportEntity(this.player.checks.POWER_ORBS) != null"
                            class="individual-entry-rank"
                        >
                            {{ getRankFromPlayerReportEntity(this.player.checks.POWER_ORBS) }}
                        </span>
                    </h4>
                    <p v-if="getPlayerReportEntity(this.player.checks.POWER_ORBS) != null">
                        {{ getHumanizedPowerOrbName(getPlayerReportEntity(this.player.checks.POWER_ORBS).metric.item) }}
                    </p>
                    <article v-else class="message is-danger">
                        <div class="message-header">
                            Inventory API is disabled!
                        </div>
                    </article>
                </div>
            </div>
        </div>
    </article>
</template>

<style lang="scss">
    .rank-status {
        padding-right: 8px;
    }
    .individual-entry-rank {
        margin-left: 8px;
        padding-left: 8px;
        border-left: solid 1px;
        font-size: 1rem;
    }
    .message-header {
        cursor: pointer;
    }
</style>

<script>
    export default {
        props: {
            player: Object,
            guild: Object,
            requirements: Object,
            getPlayerReportEntity: Function,
        },
        methods: {
            toggleCollaps(item) {
                item.collaps = !item.collaps;
                this.$forceUpdate();
            },
            getHumanizedPowerOrbName(orb) {
                switch (orb) {
                    case 'OVERFLUX':
                        return 'Overflux';
                    case 'MANA_FLUX':
                        return 'Mana Flux';
                    case 'RADIANT_ORB':
                        return 'Radiant Orb';
                }
                return orb;
            },
            getRankFromPlayerReportEntity(entry) {
                let playerReport = this.getPlayerReportEntity(entry);
                if (playerReport == null || !playerReport.hasOwnProperty('rank')) {
                    return null;
                }
                return playerReport.rank.name;
            },
            getGuildRankFromName(name) {
                if (name == 'Guild Master') {
                    return {
                        name: 'Guild Master',
                        tag: 'GM',
                        priority: 99,
                    };
                }

                for (let rank of this.guild.ranks) {
                    if (rank.name == name) {
                        return rank;
                    }
                }

                return {
                    name: 'Unknown',
                    tag: 'UK',
                    priority: -1,
                };
            },
        },
        computed: {
            currentRank() {
                return this.getGuildRankFromName(this.player.currentRank);
            },
            satisfiedRank() {
                if (! this.player.hasOwnProperty('rank')) {
                    return this.getGuildRankFromName(null);
                }
                return this.player.rank;
            },
        }
    }
</script>
