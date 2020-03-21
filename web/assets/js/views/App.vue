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
    </div>
</template>

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
