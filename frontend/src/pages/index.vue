<template>
    <h1>Teaching Allocation System</h1>
    <p>This is the Teaching Allocation System.</p>
    <p v-if="status == 'success'">The API says: {{ data!.message }}</p>
    <Loading v-else-if="status == 'pending'" />
    <ErrorPanel :error="error!" @retry="refetch" v-else>An error occurred!</ErrorPanel>
    <EmptyList>
        Brak nauczycieli
        <template #suggestion>Spróbuj zmienić filtry lub coś podobnego</template>
    </EmptyList>
</template>

<script setup lang="ts">
import { useQuery } from "@pinia/colada";
import { hello, type HelloError, type HelloResponse } from "../api/hello";
import EmptyList from "../components/EmptyList.vue";
import ErrorPanel from "../components/ErrorPanel.vue";
import Loading from "../components/Loading.vue";

const { status, data, error, refetch } = useQuery<HelloResponse, HelloError>({
    key: ["hello"],
    query: hello,
});
</script>
