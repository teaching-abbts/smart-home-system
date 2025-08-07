<template>
  <v-app id="inspire">
    <v-navigation-drawer v-model="drawer">
      <v-list-item title="My Application" subtitle="Vuetify"></v-list-item>
      <v-divider />
      <v-container>
        <v-switch
          v-model="isDarkTheme"
          true-icon="mdi-moon-waning-crescent"
          false-icon="mdi-white-balance-sunny"
          density="comfortable"
          hide-details
        />
      </v-container>
      <v-divider />
      <v-list-item link title="Home" to="/"></v-list-item>
      <v-list-item link title="Gallery" to="/gallery"></v-list-item>
      <v-list-item link title="About" to="/about"></v-list-item>
    </v-navigation-drawer>
    <v-app-bar>
      <v-app-bar-nav-icon @click="drawer = !drawer"></v-app-bar-nav-icon>
      <v-app-bar-title>Application</v-app-bar-title>
    </v-app-bar>
    <v-main>
      <v-container>
        <RouterView />
      </v-container>
    </v-main>
  </v-app>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import { useTheme } from "vuetify";

const theme = useTheme();

const drawer = ref<boolean | null>(null);
const isDarkTheme = computed({
  get: () => theme.current.value.dark,
  set: (v) => (theme.global.name.value = v ? "dark" : "light"),
});
</script>
