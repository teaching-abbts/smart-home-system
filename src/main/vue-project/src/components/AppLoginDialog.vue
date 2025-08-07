<template>
  <v-dialog max-width="500">
    <template v-slot:activator="{ props: activatorProps }">
      <v-btn v-bind="activatorProps" color="surface-variant" text="Login" variant="flat"></v-btn>
    </template>

    <template v-slot:default="{ isActive }">
      <v-card title="Dialog">
        <v-form validate-on="submit lazy" @submit.stop.prevent="onSubmit($event, isActive)">
          <v-text-field
            :rules="usernameValidationRules"
            label="User name"
            type="text"
            v-model="userName"
          />
          <v-text-field
            :rules="passwordValidationRules"
            label="Password"
            type="password"
            v-model="password"
          />
          <v-btn :loading="loading" class="mt-2" text="Submit" type="submit" block />
        </v-form>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text="Close Dialog" @click="isActive.value = false"></v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script lang="ts" setup>
import { ref, type Ref } from "vue";
import type { SubmitEventPromise } from "vuetify";
import type { FieldValidationResult } from "vuetify/lib/composables/form.mjs";
import type { ValidationResult } from "vuetify/lib/composables/validation.mjs";

type VuetifyValidationRuleset = ((value: undefined) => ValidationResult)[];

const emit = defineEmits<{
  validationFailed: [errors: FieldValidationResult[]];
  loginSuccess: [];
  loginFailure: [Error];
}>();

const loading = ref(false);
const userName = ref("");
const password = ref("");

const usernameValidationRules: VuetifyValidationRuleset = [
  (value: undefined) => value != null || "Username is required",
];
const passwordValidationRules: VuetifyValidationRuleset = [
  (value: undefined) => value != null || "Password is required",
];

async function onSubmit(submitEventPromise: SubmitEventPromise, isDialogActive: Ref<boolean>) {
  loading.value = true;
  const payload = new FormData();
  payload.append("username", userName.value);
  payload.append("password", password.value);
  try {
    const submitEvent = await submitEventPromise;

    if (!submitEvent.valid) {
      emit("validationFailed", submitEvent.errors);
      return;
    }

    const results = await fetch("/login", {
      method: "POST",
      body: payload,
    });

    if (results.ok) {
      emit("loginSuccess");
      isDialogActive.value = false;
    } else {
      const error = new Error(`Login failed: ${results.status} ${results.statusText}`);
      emit("loginFailure", error);
    }
  } catch (error) {
    emit("loginFailure", new Error(`Login failed: ${error}`));
  } finally {
    loading.value = false;
  }
}
</script>
