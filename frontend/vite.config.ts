import Vue from "@vitejs/plugin-vue";
import { defineConfig } from "vite";
import VueRouter from "vue-router/vite";

// https://vite.dev/config/
export default defineConfig({
    plugins: [VueRouter(), Vue()],
});
