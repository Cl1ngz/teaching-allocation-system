<template>
    <h1>Teaching Allocation System</h1>
    <p>This is the Teaching Allocation System.</p>
    <p>The API says: {{ message }}</p>
</template>

<script setup lang="ts">
import { useQuery } from "@pinia/colada";
import { computed } from "vue";
import { hello } from "../api/hello";

const { state: messageState } = useQuery({
    key: ["hello"],
    query: hello,
});

const message = computed(() => {
    switch (messageState.value.status) {
        case "success":
            return messageState.value.data.message;
        case "pending":
            return "(loading)";
        case "error":
            return `error: ${messageState.value.error}`;
    }
});
</script>
