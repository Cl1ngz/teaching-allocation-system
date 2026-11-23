<template>
    <Panel variant="error" class="error-panel">
        <PanelHeading class="heading">Błąd</PanelHeading>
        <p class="summary">
            {{ ErrorDictionary.translate(error, dictionary) ?? "Nieznany błąd" }}
        </p>
        <button class="retry" v-if="allowRetry" @click="retry">Spróbuj ponownie</button>
        <details class="details-accordion">
            <summary>Szczegóły</summary>
            <div class="error-details">
                <div>
                    Kod błędu: <code>{{ error.code }}</code>
                </div>
                <!-- prettier-ignore -->
                <template v-if="(error instanceof ApiError)">
                    <div>
                        ID zdarzenia: <code>{{ error.errorId }}</code>
                    </div>
                </template>
                <div>{{ error.detail }}</div>
            </div>
        </details>
    </Panel>
</template>

<script setup lang="ts">
import { ApiError, type BaseError, ErrorDictionary } from "../api/error.ts";
import Panel from "./Panel.vue";
import PanelHeading from "./PanelHeading.vue";

const {
    error,
    dictionary = ErrorDictionary.default,
    allowRetry = true,
} = defineProps<{
    error: BaseError;
    dictionary?: ErrorDictionary;
    allowRetry?: boolean;
}>();

const emit = defineEmits<{
    retry: [];
}>();

function retry() {
    emit("retry");
}
</script>

<style scoped>
.error-panel {
    display: grid;
    grid-template-columns: 1fr auto;
    grid-template-rows: repeat(3, auto);
}

.heading {
    grid-column: 1;
    grid-row: 1;
}

.retry {
    grid-column: 2;
    grid-row: 1;
}

.summary {
    grid-column: 1 / 2;
    grid-row: 2;
}

.details-accordion {
    grid-column: 1 / 2;
    grid-row: 3;
}

.error-details {
    display: flex;
    flex-flow: column nowrap;
}
</style>
