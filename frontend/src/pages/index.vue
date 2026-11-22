<template>
    <h1>Teaching Allocation System</h1>
    <p>This is the Teaching Allocation System.</p>
    <p>The API says: {{ message }}</p>
</template>

<script setup lang="ts">
import { useQuery } from "@pinia/colada";
import { computed } from "vue";
import { hello, type HelloError, type HelloResponse } from "../api/hello";

const { state: messageState } = useQuery<HelloResponse, HelloError>({
    key: ["hello"],
    query: hello,
});

const message = computed<string>(() => {
    switch (messageState.value.status) {
        case "success":
            return messageState.value.data.message;
        case "pending":
            return "(loading)";
        case "error": {
            const error = messageState.value.error;
            switch (error.code) {
                case "FETCH_FAILED":
                    return "error: Fetch failed";
                case "RESPONSE_SYNTAX_ERROR":
                    return "error: Bad response";
            }
        }
    }
});
</script>
