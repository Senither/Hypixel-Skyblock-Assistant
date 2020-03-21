<template>
    <div>
        <loading-report
            v-if="this.stage == this.stages.LOADING_REPORT"
            :id="id"
            @loaded-report="handleLoadedReport"
        />
    </div>
</template>

<script>
    import LoadingReport from './LoadingReport';

    export default {
        components: {
            LoadingReport
        },
        mounted() {
            this.id = window.location.href.split('/').pop();
            this.stage = this.stages.LOADING_REPORT;
        },
        data() {
            return {
                stage: null,
                stages: {
                    LOADING_REPORT: 0
                },
                id: null,
            };
        },
        methods: {
            handleLoadedReport(event) {
                console.log(event.report);
            }
        }
    };
</script>
