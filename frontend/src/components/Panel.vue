<template>
    <div class="panel" :class="props.variant"><slot /></div>
</template>

<script setup lang="ts">
type PanelVariant = "info" | "warning" | "error";

const props = defineProps<{
    variant: PanelVariant;
}>();
</script>

<style scoped>
.panel {
    border-radius: 0.5em;
    border: 2px solid oklch(from var(--panel-color) l calc(c * 0.75) h);
    background-color: oklch(from var(--panel-color) calc(l * 1.25) calc(c * 0.6) h);

    padding: 0.5em;
}

.panel :deep(p) {
    margin: 0.25em 0;
}

.panel.info {
    --panel-color: dodgerblue;
}
.panel.warning {
    --panel-color: goldenrod;
}
.panel.error {
    --panel-color: rgb(240, 75, 111);
}
</style>
