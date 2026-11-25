<template>
    <section class="teachers">
        <h1>Nauczyciele</h1>
        <Loading v-if="status === 'pending'" />
        <ErrorPanel :error="error!" @retry="refetch" v-else-if="status === 'error'" />
        <template v-else-if="data">
            <EmptyList v-if="data.content.length === 0">
                Brak nauczycieli
                <template #suggestion>Lista nauczycieli jest obecnie pusta.</template>
            </EmptyList>
            <template v-else>
                <p class="total">Liczba nauczycieli: {{ data.totalElements }}</p>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">Imię i nazwisko</th>
                                <th scope="col">Stopień</th>
                                <th scope="col">Jednostka</th>
                                <th scope="col">Stanowisko</th>
                                <th scope="col">E-mail</th>
                                <th scope="col">Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="teacher in data.content" :key="teacher.id">
                                <td>{{ teacher.firstName }} {{ teacher.lastName }}</td>
                                <td>{{ teacher.degreeName }}</td>
                                <td>{{ teacher.unitName ?? "Brak zatrudnienia" }}</td>
                                <td>{{ teacher.positionName ?? "—" }}</td>
                                <td><a :href="`mailto:${teacher.email}`">{{ teacher.email }}</a></td>
                                <td>{{ teacher.active ? "Aktywny" : "Nieaktywny" }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <nav class="pagination" aria-label="Stronicowanie listy nauczycieli">
                    <button :disabled="page === 0" @click="page--">Poprzednia</button>
                    <span>Strona {{ data.page + 1 }} z {{ Math.max(data.totalPages, 1) }}</span>
                    <button :disabled="page + 1 >= data.totalPages" @click="page++">
                        Następna
                    </button>
                </nav>
            </template>
        </template>
    </section>
</template>

<script setup lang="ts">
import { useQuery } from "@pinia/colada";
import { ref } from "vue";
import { listTeachers, type TeacherListError, type TeacherPage } from "../api/teachers";
import EmptyList from "../components/EmptyList.vue";
import ErrorPanel from "../components/ErrorPanel.vue";
import Loading from "../components/Loading.vue";

const page = ref(0);
const { status, data, error, refetch } = useQuery<TeacherPage, TeacherListError>({
    key: () => ["teachers", page.value],
    query: () => listTeachers(page.value),
});
</script>