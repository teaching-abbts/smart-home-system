<template>
  <v-list-item
    v-if="isLoggedIn"
    lines="two"
    prepend-avatar="https://randomuser.me/api/portraits/women/81.jpg"
    subtitle="Logged In"
    :title="userInfo?.name ?? 'Unknown User'"
  />
  <v-list-item v-else>
    <AppLoginDialog
      @login-success="onLoginSuccessAsync"
      @login-failure="onLoginFailure"
      @validation-failed="onValidationFailed"
    />
  </v-list-item>
  <v-snackbar v-model="showSnackbar">
    {{ snackbarText }}
    <template v-slot:actions>
      <v-btn color="pink" variant="text" @click="onSnackbarClose"> Close </v-btn>
    </template>
  </v-snackbar>
</template>

<script setup lang="ts">
import { computed, onBeforeMount, ref } from "vue";
import AppLoginDialog from "./AppLoginDialog.vue";
import type { FieldValidationResult } from "vuetify/lib/composables/form.mjs";

interface UserInfo {
  name: string;
  email?: string;
}

const userInfoUrl = "/user-info";

const snackbarText = ref<string | null>(null);
const showSnackbar = ref(false);
const userInfo = ref<UserInfo | null>(null);

const isLoggedIn = computed(() => {
  return userInfo.value !== null;
});

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
    const response = await fetch(userInfoUrl);

    if (!response.ok) {
      throw new Error(`${response.status}: ${response.statusText}, ${await response.text()}`);
    }

    userInfo.value = (await response.json()) as UserInfo;
  } catch (error) {
    setSnackbarMessage(`Error fetching '${userInfoUrl}': \n\n${error}`);
  }
}

async function onLoginSuccessAsync() {
  await tryFetchUserInfoAsync();
  setSnackbarMessage("Login successful!");
}

function onLoginFailure(error: Error) {
  setSnackbarMessage(`Login failed: ${error.message}`);
}

function onValidationFailed(errors: FieldValidationResult[]) {
  setSnackbarMessage(
    `Validation failed: ${errors.map((e) => e.errorMessages.join(", ")).join(", ")}`,
  );
}

onBeforeMount(async () => {
  await tryFetchUserInfoAsync();
});

defineExpose({
  tryFetchUserInfoAsync,
});
</script>
