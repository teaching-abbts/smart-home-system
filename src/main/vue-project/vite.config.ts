import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

const webhost = "http://127.0.0.1:8080";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    // https://vite.dev/config/server-options.html#server-proxy
    proxy: {
      "/image-gallery": {
        target: webhost,
        changeOrigin: true,
      },
      "^/image/.*": {
        target: webhost,
        changeOrigin: true,
      },
      "/user-info": {
        target: webhost,
        changeOrigin: true,
      },
      "/login": {
        target: webhost,
        changeOrigin: true,
      },
    },
  },
});
