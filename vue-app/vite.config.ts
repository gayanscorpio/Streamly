// WHAT: Vite config. WHY: Vite is the dev server/bundler; it needs the Vue plugin to understand .vue files.
import { defineConfig } from 'vite';          // helper that gives autocomplete for config options
import vue from '@vitejs/plugin-vue';         // teaches Vite how to compile .vue single-file components

export default defineConfig({
    plugins: [vue()],                           // enable Vue support
    server: { port: 5174 },                     // dev server port (must match allowed-origins in application.yml)
});