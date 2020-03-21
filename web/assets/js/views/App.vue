<template>
    <div>
        <loading-report
            v-if="this.stage == this.stages.LOADING_REPORT"
            :id="id"
            @loaded-report="handleLoadedReport"
        />

        <report
            v-if="this.stage == this.stages.HAS_REPORT"
            :report="report"
        />

        <footer class="footer">
            <div class="content has-text-centered">
                <p>
                    Created by <a href="https://senither.com/">Alexis Tan</a>, powered by <a href="https://bulma.io/">Bulma</a> and <a href="https://vuejs.org/">VueJS</a>, theme by <a href="https://jenil.github.io/bulmaswatch/">Bulmaswatch</a>.
                    <br>Get the <a href="https://github.com/Senither/Hypixel-Skyblock-Assistant/">source code</a> on <a href="https://github.com/Senither/Hypixel-Skyblock-Assistant/">GitHub</a>.
                </p>
            </div>
        </footer>
    </div>
</template>

<style lang="scss">
    footer.footer {
        margin-top: 48px;
    }
</style>

<script>
    import LoadingReport from './LoadingReport';
    import Report from './Report';

    export default {
        components: {
            LoadingReport,
            Report
        },
        mounted() {
            this.id = window.location.href.split('/').pop();
            this.stage = this.stages.LOADING_REPORT;
        },
        data() {
            return {
                report: null,
                stage: null,
                stages: {
                    LOADING_REPORT: 0,
                    HAS_REPORT: 1,
                },
                id: null,
            };
        },
        methods: {
            handleLoadedReport(event) {
                this.report = event.report;
                this.stage = this.stages.HAS_REPORT;
            }
        }
    };
</script>
