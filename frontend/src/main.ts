import { PiniaColada, PiniaColadaQueryHooksPlugin } from "@pinia/colada";
import { createPinia } from "pinia";
import { createApp } from "vue";
import { createRouter, createWebHistory } from "vue-router";
import { routes } from "vue-router/auto-routes";
import { DataLoaderPlugin } from "vue-router/experimental";
import { ApiError } from "./api/error.ts";
import App from "./App.vue";
import "./style.css";

const router = createRouter({
    history: createWebHistory(),
    routes,
});

createApp(App)
    // Pinia and Pinia Colada must be installed before the router: route data
    // loaders fetch through Pinia Colada queries, which require them.
    .use(createPinia())
    .use(PiniaColada, {
        plugins: [
            PiniaColadaQueryHooksPlugin({
                onError(error, _entry) {
                    if (error instanceof ApiError) {
                        console.error(
                            `Query API error '${error.code}' (${error.status}) with ID=${error.errorId}: ${error.detail}\nRaw: ${JSON.stringify(error)}`,
                        );
                    } else {
                        console.error(`Query error: ${error}`);
                    }
                },
            }),
        ],
    })
    // The data loader plugin registers navigation guards, so it must be
    // installed before the router: installing the router triggers the initial
    // navigation.
    .use(DataLoaderPlugin, { router })
    .use(router)
    .mount("#app");
