import mkcert from "vite-plugin-mkcert";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import { defineConfig } from "vite";
import { fileURLToPath, URL } from "node:url";

// const webhost = "http://127.0.0.1:8080";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    mkcert({
      savePath: "./certs",
      force: true,
    }),
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    cors: true,
    https: {
      cert: "./certs/cert.pem",
      key: "./certs/dev.pem",
    },
    // https://vite.dev/config/server-options.html#server-proxy
    // proxy: {
    //   "/image-gallery": {
    //     target: webhost,
    //     changeOrigin: true,
    //   },
    //   "^/image/.*": {
    //     target: webhost,
    //     changeOrigin: true,
    //   },
    //   "/user-info": {
    //     target: webhost,
    //     changeOrigin: true,
    //   },
    //   "/login": {
    //     target: webhost,
    //     changeOrigin: true,
    //   },
    // },
    host: "0.0.0.0",
    port: 5173,
    hmr: {
      protocol: "wss",
      host: "localhost",
      clientPort: 5173,
    },
  },
});
