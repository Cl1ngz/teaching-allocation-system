import { PiniaColada } from "@pinia/colada";
import { createPinia } from "pinia";
import { createApp } from "vue";
import { createRouter, createWebHistory } from "vue-router";
import { routes } from "vue-router/auto-routes";
import App from "./App.vue";
import "./style.css";

const router = createRouter({
    history: createWebHistory(),
    routes,
});

createApp(App).use(router).use(createPinia()).use(PiniaColada).mount("#app");
