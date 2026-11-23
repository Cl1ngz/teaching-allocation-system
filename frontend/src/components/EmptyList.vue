<template>
    <div class="list-empty">
        <div class="heading"><slot>Brak danych</slot></div>
        <div class="suggestion" v-if="$slots.suggestion"><slot name="suggestion" /></div>
    </div>
</template>

<style scoped>
.list-empty {
    display: flex;
    flex-flow: column nowrap;
    align-items: center;
    gap: 0.5em;

    .heading {
        font-weight: bold;
    }
    .suggestion {
        font-size: 0.8em;
        opacity: 0.75;
    }
}
</style>
