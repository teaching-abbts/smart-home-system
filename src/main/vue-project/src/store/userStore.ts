import { defineStore } from "pinia";
import { ref } from "vue";

export interface UserInfo {
  email?: string;
  name: string;
  roles?: string[];
  username?: string;
}

export const useUserStore = defineStore("user", () => {
  const currentUser = ref<UserInfo | null>(null);

  async function fetchUserInfoAsync(userInfoUrl: string) {
    const response = await fetch(userInfoUrl);

    if (!response.ok) {
      throw new Error(`${response.status}: ${response.statusText}, ${await response.text()}`);
    }

    const jsonResponse = (await response.json()) as UserInfo;

    currentUser.value = jsonResponse;
  }

  return {
    currentUser,
    fetchUserInfoAsync,
  };
});
