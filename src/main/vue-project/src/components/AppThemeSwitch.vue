<template>
  <v-switch
    v-model="isDarkTheme"
    true-icon="mdi-moon-waning-crescent"
    false-icon="mdi-white-balance-sunny"
    density="comfortable"
    hide-details
  />
</template>

<script setup lang="ts">
import { watchEffect } from "vue";
import { useTheme } from "vuetify";
import { useStorage } from "@vueuse/core";

const theme = useTheme();

const isDarkTheme = useStorage("isDarkTheme", theme.current.value.dark);

watchEffect(() => {
  theme.global.name.value = isDarkTheme.value ? "dark" : "light";
});
</script>
