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
                        <p class="title">3,456</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Average Slayer</p>
                        <p class="title">123</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Average Coins</p>
                        <p class="title">456K</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Average Fair Souls</p>
                        <p class="title">789</p>
                    </div>
                </div>
            </nav>

            <h3 class="title is-3 has-text-centered">Players</h3>

            <article class="message" v-for="entry of playerReports" :key="entry.uuid">
                <div class="message-header" v-on:click="toggleCollaps(entry)">
                    <div class="columns is-gapless" style="width: 100%">
                        <div class="column has-text-left">
                            [<span class="rank">Some Rank</span>]
                            {{ entry.username }}
                        </div>
                        <div class="column has-text-right uuid">{{ entry.uuid }}</div>
                    </div>
                </div>
                <div class="message-body" v-if="!entry.collaps">
                    <pre style="color:#000;border-radius: 4px">{{ entry }}</pre>
                </div>
            </article>
        </div>
    </div>
</template>

<script>
    import moment from 'moment';

    export default {
        props: {
            report: Object
        },
        mounted() {
            this.playerReports = this.report.playerReports.map(entry => {
                entry.collaps = true;
                return entry;
            });
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
        }
    }
</script>
