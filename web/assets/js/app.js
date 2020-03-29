
import Vue from 'vue';
import Axios from 'axios';

window.axios = Axios;

Vue.mixin({
    methods: {
        formatNumber(number) {
            return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        }
    }
});

import App from './views/App';

const app = new Vue({
    el: '#app',
    components: {
        App
    },
});
