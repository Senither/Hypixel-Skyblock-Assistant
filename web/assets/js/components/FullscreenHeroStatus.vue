<template>
    <section class="hero is-fullheight is-medium">
        <div class="hero-body">
            <div class="container has-text-centered">
                <h1 class="title" v-html="title"></h1>
                <h2 class="subtitle" v-html="subtitle"></h2>
                <p v-if="note != null" v-html="note"></p>
            </div>
        </div>
    </section>
</template>

<script>
    export default {
        props: {
            title: String,
            subtitle: String,
            note: String,
        },
    };
</script>
