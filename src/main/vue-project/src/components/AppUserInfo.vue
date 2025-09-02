<template>
  <v-list-item
    v-if="isLoggedIn"
    lines="two"
    prepend-avatar="https://randomuser.me/api/portraits/women/81.jpg"
    subtitle="Logged In"
    :title="userTitle"
  >
    <template v-slot:append>
      <v-btn icon="mdi-logout" size="small" variant="text" @click="onLogoutClick"></v-btn>
    </template>
  </v-list-item>
  <v-list-item v-else>
    <v-btn @click="onLoginClick" color="surface-variant" text="Login" variant="flat"> Login </v-btn>
    <!-- <AppLoginDialog
      @login-success="onLoginSuccessAsync"
      @login-failure="onLoginFailure"
      @validation-failed="onValidationFailed"
    /> -->
  </v-list-item>
  <v-snackbar v-model="showSnackbar">
    {{ snackbarText }}
    <template v-slot:actions>
      <v-btn color="pink" variant="text" @click="onSnackbarClose"> Close </v-btn>
    </template>
  </v-snackbar>
</template>

<script setup lang="ts">
import { useUserStore } from "@/store/userStore";
import { computed, onBeforeMount, ref } from "vue";
// import AppLoginDialog from "./AppLoginDialog.vue";
// import type { FieldValidationResult } from "vuetify/lib/composables/form.mjs";

const userInfoUrl = "/user-info";

const snackbarText = ref<string | null>(null);
const showSnackbar = ref(false);

const userStore = useUserStore();

const isLoggedIn = computed(() => {
  return userStore.currentUser !== null;
});

const userTitle = computed(() => userStore.currentUser?.email ?? "Unknown User");

function onSnackbarClose() {
  showSnackbar.value = false;
  snackbarText.value = null;
}

function setSnackbarMessage(message: string | null) {
  snackbarText.value = message;
  showSnackbar.value = message !== null;
}

async function tryFetchUserInfoAsync() {
  try {
    await userStore.fetchUserInfoAsync(userInfoUrl);
  } catch (error) {
    setSnackbarMessage(`Error fetching '${userInfoUrl}': \n\n${error}`);
  }
}

// async function onLoginSuccessAsync() {
//   await tryFetchUserInfoAsync();
//   setSnackbarMessage("Login successful!");
// }

// function onLoginFailure(error: Error) {
//   setSnackbarMessage(`Login failed: ${error.message}`);
// }

// function onValidationFailed(errors: FieldValidationResult[]) {
//   setSnackbarMessage(
//     `Validation failed: ${errors.map((e) => e.errorMessages.join(", ")).join(", ")}`,
//   );
// }

function onLoginClick() {
  window.location.href = `/login?redirect_uri=${encodeURIComponent(window.location.href)}`;
}

function onLogoutClick() {
  window.location.href = `/logout?post_logout_redirect_uri=${encodeURIComponent(window.location.href)}`;
}

onBeforeMount(async () => {
  await tryFetchUserInfoAsync();
});

defineExpose({
  tryFetchUserInfoAsync,
});
</script>
