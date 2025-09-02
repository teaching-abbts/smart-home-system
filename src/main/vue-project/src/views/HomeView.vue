<script setup lang="ts">
import { useUserStore } from "@/store/userStore";
import { computed, onMounted, ref } from "vue";

const userStore = useUserStore();

const message = ref<string>("");
const permission = ref<NotificationPermission | null>(null);

const username = computed(() => userStore.currentUser?.name || "unknown user");
const enableNotificationButton = computed(
  () => permission.value === "granted" && message.value.trim().length > 0,
);

function sendNotification() {
  new Notification("Welcome!", {
    body: message.value,
  });
}

onMounted(async () => {
  if ("Notification" in window) {
    permission.value = await Notification.requestPermission();
  }
});
</script>

<template>
  <v-container>
    <h1>Welcome to the Smart Home System, {{ username }}!</h1>
    <v-text-field label="Type your message" v-model="message" />
    <v-btn :disabled="!enableNotificationButton" @click="sendNotification"
      >Send Desktop Notification</v-btn
    >
  </v-container>
</template>
