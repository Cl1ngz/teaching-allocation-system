import Vue from "@vitejs/plugin-vue";
import { env } from "node:process";
import { defineConfig } from "vite";
import VueRouter from "vue-router/vite";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
    let apiUrl: string;
    if (env.TAS_API_URL) {
        apiUrl = env.TAS_API_URL;
    } else {
        if (mode == "development") {
            apiUrl = "http://localhost:8080/api";
        } else if (mode == "production") {
            apiUrl = "/api";
        } else {
            throw new Error(
                `Unknown API URL for mode ${mode}; try setting the TAS_API_URL environment variable`,
            );
        }
    }

    return {
        define: {
            API_BASE_URL: JSON.stringify(apiUrl),
        },
        plugins: [VueRouter(), Vue()],
    };
});
