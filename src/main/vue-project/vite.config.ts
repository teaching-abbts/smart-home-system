import mkcert from "vite-plugin-mkcert";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import { VitePWA } from "vite-plugin-pwa";
import { defineConfig } from "vite";
import { fileURLToPath, URL } from "node:url";

const webhost = "http://127.0.0.1:8080";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    mkcert({
      savePath: "./certs",
      force: true,
    }),
    vue(),
    VitePWA({
      registerType: "autoUpdate",
      includeAssets: [
        "favicon.ico",
        "apple-touch-icon-180x180.png",
        "maskable-icon-512x512.png",
        "pwa-640x640.png",
        "pwa-192x192.png",
        "pwa-512x512.png",
      ],
      manifest: {
        name: "My Awesome App",
        short_name: "MyApp",
        description: "My Awesome App description",
        theme_color: "#ffffff",
        icons: [
          {
            src: "pwa-192x192.png",
            sizes: "192x192",
            type: "image/png",
          },
          {
            src: "pwa-512x512.png",
            sizes: "512x512",
            type: "image/png",
          },
        ],
      },
      devOptions: {
        enabled: true,
      },
      workbox: {
        globPatterns: ["**/*.{js,css,html,ico,png,jpg,svg}"],
        runtimeCaching: [
          {
            handler: "CacheFirst",
            urlPattern: /\/image\/get\/.*/i,
            options: {
              cacheName: "image-cache",
              expiration: {
                maxEntries: 10,
                maxAgeSeconds: 60 * 60 * 24 * 365, // <== 365 days
              },
              cacheableResponse: {
                statuses: [0, 200],
              },
            },
          },
          {
            handler: "NetworkFirst",
            urlPattern: /\/image-gallery/i,
            options: {
              cacheName: "image-gallery-api-cache",
              expiration: {
                maxEntries: 5,
                maxAgeSeconds: 60 * 60 * 24 * 7, // 7 days
              },
              cacheableResponse: {
                statuses: [0, 200],
              },
              networkTimeoutSeconds: 3,
            },
          },
        ],
      },
    }),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    // cors: true,
    https: {
      cert: "./certs/cert.pem",
      key: "./certs/dev.pem",
    },
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
    host: "0.0.0.0",
    port: 5173,
    hmr: {
      protocol: "wss",
      host: "localhost",
      clientPort: 5173,
    },
  },
});
