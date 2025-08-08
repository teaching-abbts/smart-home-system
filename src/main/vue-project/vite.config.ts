import basicSsl from "@vitejs/plugin-basic-ssl";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import { defineConfig } from "vite";
import { fileURLToPath, URL } from "node:url";

const webhost = "http://127.0.0.1:8443";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    basicSsl({
      /** name of certification */
      name: "localhost",
      /** custom trust domains */
      domains: ["localhost"],
      /** custom certification directory */
      certDir: "./cert",
    }),
  ],
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
    https: {
      cert: "./cert/_cert.pem",
    },
  },
});
