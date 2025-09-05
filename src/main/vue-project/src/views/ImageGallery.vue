<template>
  <div>
    <div v-if="isOffline" class="offline-banner">
      🔌 Offline-Modus - Zeige zwischengespeicherte Bilder
    </div>
    <p>
      <FileInput v-model="uploadFiles" :accept="['image/png', 'image/jpeg']" multiple />
      <button class="mr-2" @click="uploadImagesAsync" :disabled="isOffline">⬆️ Hochladen</button>
      <button @click="loadImageGalleryAsync">🔄️ Nachladen</button>
      <span v-if="isLoading" class="loading-indicator">⏳ Lade...</span>
    </p>
    <div v-if="imageGallery.images.length > 0">
      <div
        :key="index"
        :style="`background-image: url(${image.url})`"
        :class="['image', { 'image-loaded': loadedImages.has(image.url) }]"
        v-for="(image, index) in imageGallery.images"
        :title="image.name"
      >
        <button @click="deleteImageAsync(index)" :disabled="isOffline">⛔ Löschen</button>
      </div>
    </div>
    <h1 v-else-if="!isLoading">No Images... 😢</h1>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, onUnmounted } from "vue";
import FileInput from "@/components/FileInput.vue";

interface Image {
  url: string;
  name: string;
}

interface ImageGalleryResult {
  images: Image[];
}

const uploadFiles = ref<File[]>([]);
const isOffline = ref(!navigator.onLine);
const isLoading = ref(false);

const imageGallery = ref<ImageGalleryResult>({
  images: [],
});

const loadedImages = ref<Set<string>>(new Set());

// Listen for online/offline events
function updateOnlineStatus() {
  isOffline.value = !navigator.onLine;
}

onMounted(() => {
  window.addEventListener("online", updateOnlineStatus);
  window.addEventListener("offline", updateOnlineStatus);
});

onUnmounted(() => {
  window.removeEventListener("online", updateOnlineStatus);
  window.removeEventListener("offline", updateOnlineStatus);
});

function preloadImage(url: string): Promise<void> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => {
      loadedImages.value.add(url);
      resolve();
    };
    img.onerror = reject;
    img.src = url;
  });
}

async function uploadImagesAsync() {
  if (isOffline.value) {
    alert("Uploads sind im Offline-Modus nicht möglich");
    return;
  }

  if (uploadFiles.value.length > 0) {
    try {
      const formData = new FormData();

      uploadFiles.value.forEach((file) => {
        formData.append(`file[${file.name}]`, file);
      });

      const response = await fetch("image/upload", {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        alert(response.statusText);
      }
    } catch (error) {
      alert(error);
    }
  }

  uploadFiles.value = [];
  await loadImageGalleryAsync();
}

async function getImageGalleryAsync() {
  try {
    const response = await fetch("/image-gallery");

    if (response.ok) {
      return (await response.json()) as ImageGalleryResult;
    }

    throw new Error(response.statusText);
  } catch (error) {
    // If we're offline, try to show cached data instead of an alert
    if (!navigator.onLine) {
      console.log("Offline: Versuche zwischengespeicherte Daten zu verwenden");
      // The service worker will handle serving from cache
      // If it fails, we'll fall back to empty images array
    } else {
      // alert(error);
    }

    return {
      images: [],
    };
  }
}

async function deleteImageAsync(index: number) {
  if (isOffline.value) {
    alert("Löschen ist im Offline-Modus nicht möglich");
    return;
  }

  try {
    const image = imageGallery.value.images[index];
    if (!image) {
      throw new Error("Image not found");
    }
    const imageFullName = image.url.split("/").pop();
    const response = await fetch(`/image/delete/${imageFullName}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }
  } catch (error) {
    alert(error);
  } finally {
    await loadImageGalleryAsync();
  }
}

async function loadImageGalleryAsync() {
  isLoading.value = true;

  try {
    imageGallery.value = await getImageGalleryAsync();

    // Preload all images
    const preloadPromises = imageGallery.value.images.map((image) =>
      preloadImage(image.url).catch(console.error),
    );

    await Promise.allSettled(preloadPromises);
  } finally {
    isLoading.value = false;
  }
}

onMounted(async () => {
  await loadImageGalleryAsync();
});
</script>

<style scoped>
.offline-banner {
  background-color: #ff9800;
  color: white;
  padding: 10px;
  text-align: center;
  border-radius: 4px;
  margin-bottom: 20px;
  font-weight: bold;
}

.loading-indicator {
  margin-left: 10px;
  color: #666;
  font-style: italic;
}

.image {
  display: inline-block;
  width: 300px;
  height: 300px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  margin: 10px;
  border: 2px solid #ddd;
  border-radius: 8px;
  transition: opacity 0.3s ease;
}

.image:not(.image-loaded) {
  opacity: 0.5;
  background-color: #f0f0f0;
}

.image-loaded {
  opacity: 1;
}

.image button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
  font-size: 12px;
}

.image button:hover {
  background: rgba(255, 255, 255, 1);
}

.image button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
