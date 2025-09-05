<template>
  <div>
    <p>
      <FileInput v-model="uploadFiles" :accept="['image/png', 'image/jpeg']" multiple />
      <button @click="uploadImagesAsync">⬆️ Hochladen</button>
    </p>
    <p>
      <button @click="loadImageGalleryAsync">🔄️ Nachladen</button>
    </p>
    <div v-if="imageGallery.images.length > 0">
      <div
        :key="index"
        :style="`background-image: url(${image.url})`"
        :class="['image', { 'image-loaded': loadedImages.has(image.url) }]"
        v-for="(image, index) in imageGallery.images"
        :title="image.name"
      >
        <button @click="deleteImageAsync(index)">⛔ Löschen</button>
      </div>
    </div>
    <h1 v-else>No Images... 😢</h1>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue";
import FileInput from "@/components/FileInput.vue";

interface Image {
  url: string;
  name: string;
}

interface ImageGalleryResult {
  images: Image[];
}

const uploadFiles = ref<File[]>([]);

const imageGallery = ref<ImageGalleryResult>({
  images: [],
});

const loadedImages = ref<Set<string>>(new Set());

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
    alert(error);

    return {
      images: [],
    };
  }
}

async function deleteImageAsync(index: number) {
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
  imageGallery.value = await getImageGalleryAsync();

  // Preload all images
  const preloadPromises = imageGallery.value.images.map((image) =>
    preloadImage(image.url).catch(console.error),
  );

  await Promise.allSettled(preloadPromises);
}

onMounted(async () => {
  await loadImageGalleryAsync();
});
</script>

<style scoped>
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
</style>
