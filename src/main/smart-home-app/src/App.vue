<template>
  <v-app>
    <v-app-bar>
      <v-app-bar-nav-icon @click="drawer = !drawer" />
      <v-app-bar-title>Smart Home App</v-app-bar-title>
    </v-app-bar>
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script lang="ts" setup>
  import { ref } from 'vue'

  const drawer = ref<boolean | null>(null)
</script>
