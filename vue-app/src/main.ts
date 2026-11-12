// WHAT: Front-end entry point. WHY: creates the Vue app and attaches it to the page.
import { createApp } from 'vue';        // factory that creates a Vue application
import App from './App.vue';            // the root component (everything else is nested inside it)
import './styles.css';                  // load global styles once

createApp(App).mount('#app');           // render <App> inside <div id="app"> from index.html