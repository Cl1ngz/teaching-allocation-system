import Vue from "@vitejs/plugin-vue";
import { defineConfig } from "vite";
import VueRouter from "vue-router/vite";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
    let apiUrl: string;
    if (mode == "development") {
        // TODO: substitute with wherever the local server ends up
        apiUrl = "http://localhost:3000/";
    } else {
        throw new Error("Unknown API URL for mode " + mode);
    }

    return {
        define: {
            API_BASE_URL: JSON.stringify(apiUrl),
        },
        plugins: [VueRouter(), Vue()],
    };
});
