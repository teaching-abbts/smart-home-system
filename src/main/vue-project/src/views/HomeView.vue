<script setup lang="ts">
import { useUserStore } from "@/store/userStore";
import { computed } from "vue";

const userStore = useUserStore();

const username = computed(() => userStore.currentUser?.name || "unknown user");
</script>

<template>
  <h1>Welcome to the Smart Home System, {{ username }}!</h1>
</template>
