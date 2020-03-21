<template>
    <section class="hero is-fullheight is-medium" v-if="isValidReportId">
        <div class="hero-body">
            <div class="container has-text-centered">
                <h1 class="title">
                    Loading guild scan report
                </h1>
                <h2 class="subtitle">
                    Report ID: <i>{{ id }}</i>
                </h2>

                <p class="button is-loading"></p>
            </div>
        </div>
    </section>

    <section class="hero is-fullheight is-medium" v-else>
        <div class="hero-body">
            <div class="container has-text-centered">
                <h1 class="title">
                    Failed to load guild scan report
                </h1>
                <h2 class="subtitle">
                    Invalid report ID provided!
                </h2>
            </div>
        </div>
    </section>
</template>

<style lang="scss">
    .button.is-loading {
        font-size: 8em;
        border-color: rgba(0, 0, 0, 0);
        background-color: rgba(0, 0, 0, 0);
    }
</style>

<script>
    export default {
        props: {
            id: String
        },
        mounted() {
            this.isValidReportId = this.isUuid(this.id);
        },
        data() {
            return {
                isValidReportId: true,
            };
        },
        methods: {
            isUuid(uuid) {
                return uuid.match(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i) != null;
            }
        }
    }
</script>
