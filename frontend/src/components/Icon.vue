<template>
    <svg class="icon" role="presentation"><use :href="'/icons.svg#' + name" /></svg>
</template>

<script setup lang="ts">
const ICONS = {
    loading: { viewBox: "0 0 120 30" },
} as const;

type IconName = keyof typeof ICONS;

const props = defineProps<{
    name: IconName;
}>();
</script>

<style scoped>
.icon {
    width: 2em;
    height: 2em;
}
</style>
