<template>
    <section class="teachers">
        <h1>Nauczyciele</h1>
        <Loading v-if="isLoading" />
        <ErrorPanel :error="error!" @retry="() => refetch()" v-else-if="error" />
        <template v-else-if="data">
            <EmptyList v-if="data.content.length === 0">
                Brak nauczycieli
                <template #suggestion>Lista nauczycieli jest obecnie pusta.</template>
            </EmptyList>
            <template v-else>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">Imię i nazwisko</th>
                                <th scope="col">Stopień</th>
                                <th scope="col">Jednostka</th>
                                <th scope="col">Stanowisko</th>
                                <th scope="col">Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr
                                v-for="teacher in data.content"
                                :key="teacher.id"
                                :class="{ inactive: !teacher.active }"
                            >
                                <td>{{ teacher.firstName }} {{ teacher.lastName }}</td>
                                <td>{{ teacher.degreeName }}</td>
                                <td>{{ teacher.unitName ?? "Brak zatrudnienia" }}</td>
                                <td>{{ teacher.positionName ?? "-" }}</td>
                                <td>{{ teacher.active ? "Aktywny" : "Nieaktywny" }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <nav class="pagination" aria-label="Stronicowanie listy nauczycieli">
                    <button :disabled="page === 0" @click="goToPage(page - 1)">Poprzednia</button>
                    <span>Strona {{ data.page + 1 }} z {{ Math.max(data.totalPages, 1) }}</span>
                    <button :disabled="page + 1 >= data.totalPages" @click="goToPage(page + 1)">
                        Następna
                    </button>
                </nav>
            </template>
        </template>
    </section>
</template>

<script lang="ts">
import type { LocationQuery } from "vue-router";
import { defineColadaLoader } from "vue-router/experimental/pinia-colada";
import { listTeachers } from "../api/teachers";

/**
 * Reads the zero-based page number from the route query.
 * Falls back to the first page when the parameter is missing or invalid.
 */
function pageFromQuery(query: LocationQuery): number {
    const raw = query.page;
    const value = Number(Array.isArray(raw) ? raw[0] : raw);
    return Number.isInteger(value) && value >= 0 ? value : 0;
}

export const useTeacherList = defineColadaLoader("/teachers", {
    // the default non-lazy behavior means the router waits for loading to continue,
    // but we want our loading/error states on the page
    lazy: true,
    key: (to) => ["teachers", pageFromQuery(to.query)],
    query: (to) => listTeachers(pageFromQuery(to.query)),
});
</script>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import EmptyList from "../components/EmptyList.vue";
import ErrorPanel from "../components/ErrorPanel.vue";
import Loading from "../components/Loading.vue";

const { data, error, isLoading, refetch } = useTeacherList();

const route = useRoute("/teachers");
const router = useRouter();

const page = computed(() => pageFromQuery(route.query));

function goToPage(next: number) {
    router.push({ query: { ...route.query, page: next === 0 ? undefined : String(next) } });
}
</script>

<style scoped>
tr.inactive {
    opacity: 0.5;
}
</style>
