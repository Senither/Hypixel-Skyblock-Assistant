<template>
    <article class="message">
        <div class="message-header" v-on:click="toggleCollaps(player)">
            <div class="columns is-gapless" style="width: 100%">
                <div class="column has-text-left">
                    [<span class="rank">{{ getCurrentRankForUUID(player.uuid) }}</span>]
                    {{ player.username }}
                </div>
                <div class="column has-text-right uuid">{{ player.uuid }}</div>
            </div>
        </div>
        <div class="message-body" v-if="!player.collaps">
            <pre style="color:#000;border-radius: 4px">{{ player }}</pre>
        </div>
    </article>
</template>

<script>
    export default {
        props: {
            player: Object,
            guild: Object,
        },
        methods: {
            toggleCollaps(item) {
                item.collaps = !item.collaps;
                this.$forceUpdate();
            },
            getCurrentRankForUUID(uuid) {
                for (let member of this.guild.members) {
                    if (member.uuid == uuid) {
                        return member.rank;
                    }
                }
                return 'Unknown';
            }
        }
    }
</script>
