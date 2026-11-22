import Vue from "@vitejs/plugin-vue";
import { cwd } from "node:process";
import { defineConfig, loadEnv } from "vite";
import VueRouter from "vue-router/vite";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, cwd(), "");
  const apiUrl = env.TAS_API_URL || "/api";
  const backendTarget = env.TAS_BACKEND_URL || "http://localhost:8080";

  return {
    define: {
      API_BASE_URL: JSON.stringify(apiUrl),
    },
    server: {
      proxy: {
        "/api": {
          target: backendTarget,
          changeOrigin: true,
        },
      },
    },
    plugins: [VueRouter(), Vue()],
  };
});
