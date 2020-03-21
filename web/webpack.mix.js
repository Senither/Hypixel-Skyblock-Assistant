const mix = require('laravel-mix');

mix.js('assets/js/app.js', 'public/js')
   .sass('assets/sass/app.scss', 'public/css');
