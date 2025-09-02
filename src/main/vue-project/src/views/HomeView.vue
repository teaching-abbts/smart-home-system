<script setup lang="ts">
import { useUserStore } from "@/store/userStore";
import TheWelcome from "../components/TheWelcome.vue";

const userStore = useUserStore();
</script>

<template>
  <h1 v-if="userStore.currentUser">Welcome, {{ userStore.currentUser.name }}!</h1>
  <TheWelcome />
</template>
