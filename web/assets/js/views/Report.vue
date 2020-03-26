<template>
    <div>
        <section class="hero has-text-centered is-dark">
            <div class="hero-body">
                <div class="container">
                    <h1 class="title">
                        {{ report.guildEntry.name }} Report
                    </h1>
                    <h2 class="subtitle">
                        {{ friendlyCreatedAtTime }}
                        <br>Scanned {{ report.playerReports.length }} players in {{ timeTakenToCompleteScan }}!
                    </h2>
                </div>
            </div>
        </section>

        <div class="report-container container">
            <h3 class="title is-3 has-text-centered">Guild Statistics</h3>
            <nav class="level">
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Average Skill Level</p>
                        <p class="title">{{ guildAverages.skill.value }}</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div :data-tooltip="`${guildAverages.slayer.users} users in the guild has a combined ${guildAverages.slayer.total} slayer XP`">
                        <p class="heading">Average Slayer</p>
                        <p class="title">{{ guildAverages.slayer.value }}</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div :data-tooltip="`${guildAverages.coins.users} users in the guild has a combined ${guildAverages.coins.total} coins`">
                        <p class="heading">Average Coins</p>
                        <p class="title">{{ guildAverages.coins.value }}</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div :data-tooltip="`${guildAverages.fairy.users} users in the guild has a combined ${guildAverages.fairy.total} fairy souls`">
                        <p class="heading">Average Fairy Souls</p>
                        <p class="title">{{ guildAverages.fairy.value }}</p>
                    </div>
                </div>
            </nav>

            <h3 class="title is-3 has-text-centered">Players</h3>

            <player-report
                v-for="entry of playerReports"
                :key="entry.uuid"
                :player="entry"
                :guild="report.guildReply.guild"
                :requirements="report.guildEntry.rankRequirements"
                :getPlayerReportEntity="getResponseFromPlayerReportEntity"
            />
        </div>
    </div>
</template>

<script>
    import moment from 'moment';
    import PlayerReport from '../components/PlayerReport';

    export default {
        props: {
            report: Object
        },
        components: {
            PlayerReport
        },
        mounted() {
            let playerRankGroups = { 'Guild Master': [] };
            this.report.guildReply.guild.ranks.sort((a, b) => {
                return a.priority < b.priority ? 1 : -1;
            }).forEach(rank => playerRankGroups[rank.name] = []);

            this.report.guildReply.guild.members.forEach(player => {
                for (let member of this.report.playerReports) {
                    if (member.uuid == player.uuid) {
                        playerRankGroups[player.rank].push(member);
                        continue;
                    }
                }
            });

            for (let rank of Object.keys(playerRankGroups)) {
                playerRankGroups[rank].sort((a, b) => {
                    return a.username > b.username ? 1 : -1;
                }).forEach(player => {
                    player.collaps = true;
                    player.currentRank = rank;

                    this.playerReports.push(player);
                });
            }
        },
        data() {
            return {
                playerReports: []
            };
        },
        methods: {
            toggleCollaps(item) {
                item.collaps = !item.collaps;
                this.$forceUpdate();
            },
            getResponseFromPlayerReportEntity(reportEntity) {
                if (! reportEntity.metric.hasOwnProperty('exception')) {
                    return reportEntity;
                }

                if (! reportEntity.metric.exception.hasOwnProperty('rankResponse')) {
                    return null;
                }
                return reportEntity.metric.exception.rankResponse;
            }
        },
        computed: {
            friendlyCreatedAtTime() {
                return moment(this.report.created_at).format('dddd, Do of MMMM, YYYY - HH:mm:ss zz');
            },
            timeTakenToCompleteScan() {
                return moment.duration(
                    moment(this.report.finished_at).diff(
                        moment(this.report.created_at)
                    )
                ).humanize();
            },
            guildAverages() {
                let averages = {
                    skill: { value: 0, users: 0, type: 'AVERAGE_SKILLS' },
                    slayer: { value: 0, users: 0, type: 'SLAYER' },
                    coins: { value: 0, users: 0, type: 'BANK' },
                    fairy: { value: 0, users: 0, type: 'FAIRY_SOULS' },
                };

                for (let player of this.report.playerReports) {
                    for (let typeId of Object.keys(averages)) {
                        let playerReportType = this.getResponseFromPlayerReportEntity(
                            player.checks[averages[typeId].type]
                        );

                        if (playerReportType == null) {
                            continue;
                        }


                        let property = typeId == 'coins' ? 'bank' : 'amount';
                        if (playerReportType.metric[property] < 1) {
                            continue;
                        }

                        averages[typeId].users += 1;
                        averages[typeId].value += playerReportType.metric[property];
                    }
                }

                for (let typeId of Object.keys(averages)) {
                    averages[typeId].total = this.formatNumber(averages[typeId].value);
                    averages[typeId].value = this.formatNumber(
                        (averages[typeId].value / averages[typeId].users).toFixed(2)
                    );
                }

                return averages;
            }
        }
    }
</script>
